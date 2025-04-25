package ru.hse.pensieve.search.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.search.service.SearchService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        try {
            return ResponseEntity.ok(searchService.searchUsers(query));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
