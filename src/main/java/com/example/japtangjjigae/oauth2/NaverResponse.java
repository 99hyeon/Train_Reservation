package com.example.japtangjjigae.oauth2;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    @SuppressWarnings("unchecked")
    public NaverResponse(Map<String, Object> attributes) {
        Object response = attributes.get("response");

        if(response instanceof Map<?, ?> map){
            this.attributes = (Map<String, Object>) map;
        } else {
            this.attributes = Map.of();
        }
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id == null ? null : id.toString();
    }

}
