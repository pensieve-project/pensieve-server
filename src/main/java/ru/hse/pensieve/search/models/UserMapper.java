package ru.hse.pensieve.search.models;

import ru.hse.pensieve.database.elk.elasticsearch.models.EsUserDocument;

public class UserMapper {
    public static UserResponse fromEs(EsUserDocument esUserDocument) {
        return new UserResponse(
                esUserDocument.getUserId(),
                esUserDocument.getUsername()
        );
    }
}
