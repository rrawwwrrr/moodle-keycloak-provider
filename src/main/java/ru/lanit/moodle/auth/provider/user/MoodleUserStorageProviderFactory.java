package ru.lanit.moodle.auth.provider.user;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

import static ru.lanit.moodle.auth.provider.MoodleUtil.getConfigMenu;

public class MoodleUserStorageProviderFactory implements UserStorageProviderFactory<MoodleUserStorageProvider> {
    protected final List<ProviderConfigProperty> configMetadata = getConfigMenu();
    public static final String PROVIDER_ID = "moodle";


    @Override
    public MoodleUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new MoodleUserStorageProvider(session, model);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}