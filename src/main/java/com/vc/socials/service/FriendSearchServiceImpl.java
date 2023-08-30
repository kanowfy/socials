package com.vc.socials.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import com.vc.socials.esmodel.FriendDoc;
import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepository;

@Service
public class FriendSearchServiceImpl implements FriendSearchService {
    private RestHighLevelClient client;
    private UserRepository userRepository;
    private static final String FRIEND_INDEX = "friends_soc";

    public FriendSearchServiceImpl(RestHighLevelClient client, UserRepository userRepository) {
        this.client = client;
        this.userRepository = userRepository;
    }

    @Override
    public List<User> searchForFriends(Long currentUserId, String term) throws IOException {
        // build query
        BoolQueryBuilder user1IsCurrent = QueryBuilders.boolQuery()
                .must(new TermQueryBuilder("user1_id", currentUserId))
                .must(new MatchQueryBuilder("user2_fullname", term));
        BoolQueryBuilder user2IsCurrent = QueryBuilders.boolQuery()
                .must(new TermQueryBuilder("user2_id", currentUserId))
                .must(new MatchQueryBuilder("user1_fullname", term));

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().should(user1IsCurrent).should(user2IsCurrent);

        // construct new search source
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);

        // construct new search request
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(FRIEND_INDEX);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        List<User> users = new ArrayList<>();

        // process search hit
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Long user1Id = Long.valueOf(sourceAsMap.get("user1_id").toString());
            Long user2Id = Long.valueOf(sourceAsMap.get("user2_id").toString());
            if (user1Id == currentUserId) {
                User user = userRepository.findById(user2Id).get();
                users.add(user);
            } else {
                User user = userRepository.findById(user1Id).get();
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public void createFriendIndex(FriendDoc friend) throws IOException {
        IndexRequest request = new IndexRequest(FRIEND_INDEX);
        request.id(UUID.randomUUID().toString());

        Map<String, Object> objMap = new HashMap<>();
        objMap.put("user1_id", friend.getUser1Id());
        objMap.put("user2_id", friend.getUser2Id());
        objMap.put("user1_fullname", friend.getUser1Fullname());
        objMap.put("user2_fullname", friend.getUser2Fullname());

        request.source(objMap);
        client.index(request, RequestOptions.DEFAULT);
    }
}
