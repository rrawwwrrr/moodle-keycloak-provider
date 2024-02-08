package ru.lanit.moodle.auth.provider.group;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MoodleGroupModel implements GroupModel {
    private String id;
    private String name;
    private Map<String, List<String>> attributes;

    public  MoodleGroupModel(String id, String name) {
        attributes = new HashMap<>();
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        attributes.put(name, List.of(value));
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        attributes.put(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getFirstAttribute(String name) {
        return attributes.get(name).get(0);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return attributes.get(name).stream();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    @Override
    public GroupModel getParent() {
        return null;
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public Stream<GroupModel> getSubGroupsStream() {
        return Stream.empty();
    }

    @Override
    public void setParent(GroupModel group) {

    }

    @Override
    public void addChild(GroupModel subGroup) {

    }

    @Override
    public void removeChild(GroupModel subGroup) {

    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return null;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public void grantRole(RoleModel role) {

    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return null;
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {

    }
}