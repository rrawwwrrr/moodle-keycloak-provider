package ru.lanit.moodle.auth.provider.group;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.group.GroupStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.moodle.auth.provider.MoodleUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MoodleGroupProvider implements GroupProvider, GroupStorageProvider {
    private static final Logger log = LoggerFactory.getLogger(MoodleGroupProvider.class);
    private KeycloakSession session;
    private ComponentModel model;

    public MoodleGroupProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void close() {
        log.info("[I30] close()");
    }

    @Override
    public Stream<GroupModel> getGroupsStream(RealmModel realm) {
        log.info("[I139] getGroupsStream: realm={}; ", realm.getName());
        return getGroupsStream(realm, null, 0, 9999);
    }

    @Override
    public Stream<GroupModel> getGroupsStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        log.info("[I139] getGroupsStream: realm={};search={};first={},max={}", realm.getName(), search, max);

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select id, idnumber from public.mdl_cohort where visible = 1 limit ? offset ?");
            st.setString(1, search);
            st.setInt(2, max);
            st.setInt(3, first);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<GroupModel> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new MoodleGroupModel(rs.getString("id"), rs.getString("idnumber")));
            }
            return users.stream();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Long getGroupsCount(RealmModel realm, Boolean onlyTopGroups) {
        log.info("[I139] getGroupsCount: realm={};onlyTopGroups={} ", realm.getName(), onlyTopGroups);

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            Statement st = c.createStatement();
            st.execute("select count(*) from public.mdl_cohort where visible = 1");
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Long getGroupsCountByNameContaining(RealmModel realm, String search) {
        log.info("[I139] getGroupsCountByNameContaining: realm={};search={} ", realm.getName(), search);

        try (Connection c = MoodleUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select count(*) from public.mdl_cohort where idnumber like ?");
            st.setString(1, search);
            st.execute();
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Stream<GroupModel> getGroupsByRoleStream(RealmModel realm, RoleModel role, Integer firstResult, Integer maxResults) {
        log.info("[I139] getGroupsByRoleStream: realm={};role={};firstResult={},maxResults={}  ", realm.getName(), role.getName(), firstResult, maxResults);
        return null;
    }

    @Override
    public Stream<GroupModel> getTopLevelGroupsStream(RealmModel realm, String search, Boolean exact, Integer firstResult, Integer maxResults) {
        log.info("[I139] getTopLevelGroupsStream: realm={};search={};firstResult={},maxResults={}  ", realm.getName(), search, firstResult, maxResults);
        return null;
    }

    @Override
    public GroupModel createGroup(RealmModel realm, String id, String name, GroupModel toParent) {
        log.info("группа создана", name);
        return new MoodleGroupModel(id, name);
    }

    @Override
    public boolean removeGroup(RealmModel realm, GroupModel group) {
        log.info("группа удалена", group.getName());
        return false;
    }

    @Override
    public void moveGroup(RealmModel realm, GroupModel group, GroupModel toParent) {
        log.info("группа перемещена", group.getName());

    }

    @Override
    public void addTopLevelGroup(RealmModel realm, GroupModel subGroup) {
        log.info("addTopLevelGroup");
    }

    @Override
    public GroupModel getGroupById(RealmModel realm, String id) {
        log.info("getGroupById");
        return null;
    }

    @Override
    public Stream<GroupModel> searchGroupsByAttributes(RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
        log.info("searchGroupsByAttributes");

        return Stream.empty();
    }

    @Override
    public Stream<GroupModel> searchForGroupByNameStream(RealmModel realm, String search, Boolean exact, Integer firstResult, Integer maxResults) {
        log.info("searchForGroupByNameStream");

        return Stream.empty();
    }
}
