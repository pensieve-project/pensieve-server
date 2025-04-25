package ru.hse.pensieve.search.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.config.exceptions.ErrorResponse;
import ru.hse.pensieve.search.models.EsNotFoundException;
import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.search.service.SearchService;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchUsers(query));
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> searchThemes(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchThemes(query));
    }

    @ExceptionHandler(EsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEsNotFoundException(Exception ex) {
        log.error("Failed to search in ElasticSearch: ", ex);

        ErrorResponse error = new ErrorResponse(
                "Failed to search in ElasticSearch: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
