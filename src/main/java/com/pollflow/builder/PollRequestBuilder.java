package com.pollflow.builder;

import com.pollflow.dto.PollRequest;
import java.util.ArrayList;
import java.util.List;

public class PollRequestBuilder {
    private String title;
    private String description;
    private Long categoryId;
    private List<String> options = new ArrayList<>();

    public PollRequestBuilder title(String title) {
        this.title = title;
        return this;
    }

    public PollRequestBuilder description(String description) {
        this.description = description;
        return this;
    }

    public PollRequestBuilder categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public PollRequestBuilder option(String option) {
        this.options.add(option);
        return this;
    }

    public PollRequestBuilder options(List<String> options) {
        this.options = options;
        return this;
    }

    public PollRequest build() {
        PollRequest request = new PollRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        request.setOptions(options);
        return request;
    }
}
