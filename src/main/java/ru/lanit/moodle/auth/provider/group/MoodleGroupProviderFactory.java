package ru.lanit.moodle.auth.provider.group;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.group.GroupStorageProviderFactory;

import java.util.List;

import static ru.lanit.moodle.auth.provider.MoodleUtil.getConfigMenu;

public class MoodleGroupProviderFactory implements GroupStorageProviderFactory<MoodleGroupProvider> {
    protected final List<ProviderConfigProperty> configMetadata = getConfigMenu();

    public static final String PROVIDER_ID = "moodle-group";
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public MoodleGroupProvider create(KeycloakSession session, ComponentModel model) {
        return new MoodleGroupProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
