package ru.lanit.moodle.auth.provider.user;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.moodle.auth.provider.MoodleUtil;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoodleUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {
    private static final Logger log = LoggerFactory.getLogger(MoodleUserStorageProvider.class);
    private KeycloakSession session;
    private ComponentModel model;

    public MoodleUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        log.info("[I30] close()");
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log.info("[I35] getUserById({})", id);
        StorageId sid = new StorageId(id);
        return getUserByUsername(realm, sid.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log.info("[I41] getUserByUsername({})", username);
        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email from public.mdl_user where username = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return mapUser(realm, rs);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log.info("[I48] getUserByEmail({})", email);
        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email from public.mdl_user where email = ?");
            st.setString(1, email);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                return mapUser(realm, rs);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        log.info("[I57] supportsCredentialType({})", credentialType);
        return PasswordCredentialModel.TYPE.endsWith(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        log.info("[I57] isConfiguredFor(realm={},user={},credentialType={})", realm.getName(), user.getUsername(), credentialType);
        // In our case, password is the only type of credential, so we allways return 'true' if
        // this is the credentialType
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        log.info("[I57] isValid(realm={},user={},credentialInput.type={})", realm.getName(), user.getUsername(), credentialInput.getType());
        if (!this.supportsCredentialType(credentialInput.getType())) {
            return false;
        }
        StorageId sid = new StorageId(user.getId());
        String username = sid.getExternalId();

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select password from public.mdl_user where username = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                String pwd = rs.getString(1);
                return pwd.equals(credentialInput.getChallengeResponse());
            } else {
                return false;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }


    @Override
    public int getUsersCount(RealmModel realm) {
        log.info("[I48] getUsersCount()");
        try (Connection c = MoodleUtil.getConnection(this.model)) {
            Statement st = c.createStatement();
            st.execute("select count(*) from public.mdl_user");
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        log.info("[I113] getGroupMembersStream: realm={}", realm.getName());

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email from public.mdl_user order by username limit ? offset ?");
            st.setInt(1, maxResults);
            st.setInt(2, firstResult);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(realm, rs));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        log.info("[I139] searchForUserStream: realm={}", realm.getName());

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email from users where username like ? order by username limit ? offset ?");
            st.setString(1, search);
            st.setInt(2, maxResults);
            st.setInt(3, firstResult);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(realm, rs));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        log.info("[I139] searchForUserStream: realm={}; attrName={};", realm.getName(), params.entrySet().stream().map(Objects::toString).collect(Collectors.joining(";")));
        try (Connection c = MoodleUtil.getConnection(this.model)) {
            String where = "";
            String paramWhere = params.get("keycloak.session.realm.users.query.search");
            if (paramWhere != null && !paramWhere.isEmpty()) {
                where += " where username like '%" + paramWhere + "%'";
            }
            PreparedStatement st = c.prepareStatement("select username, firstName,lastName, email from public.mdl_user " + where + "order by username limit ? offset ?");
            st.setInt(1, maxResults);
            st.setInt(2, firstResult);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(realm, rs));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        log.info("[I139] searchForUserByUserAttributeStream: realm={}; attrName={};", realm.getName(), attrName);

        return Stream.empty();
    }

    private UserModel mapUser(RealmModel realm, ResultSet rs) throws SQLException {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        MoodleUser user = new MoodleUser.Builder(session, realm, model, rs.getString("username"))
                .email(rs.getString("email"))
                .firstName(rs.getString("firstname"))
                .lastName(rs.getString("lastname"))
                .build();

        return user;
    }
}
