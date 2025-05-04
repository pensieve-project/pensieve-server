package ru.hse.pensieve.search.service;

import ru.hse.pensieve.search.models.EsNotFoundException;
import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.util.List;

public interface SearchService {

    List<UserResponse> searchUsers(String prefix) throws EsNotFoundException;

    List<ThemeResponse> searchThemes(String query) throws EsNotFoundException;
}
