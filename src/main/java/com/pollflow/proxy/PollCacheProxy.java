package com.pollflow.proxy;

import com.pollflow.dto.PollDTO;
import com.pollflow.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PollCacheProxy {
    private final PollService pollService;
    
    public Page<PollDTO> getAllPolls(Pageable pageable, Long userId) {
        String cacheKey = "polls_page_" + pageable.getPageNumber() + "_size_" + pageable.getPageSize();
        
        if (Cache.contains(cacheKey)) {
            return (Page<PollDTO>) Cache.get(cacheKey);
        }
        
        Page<PollDTO> polls = pollService.getAllPolls(pageable, userId);
        Cache.put(cacheKey, polls);
        return polls;
    }
    
    public PollDTO getPollById(Long id, Long userId) {
        String cacheKey = "poll_" + id;
        
        if (Cache.contains(cacheKey)) {
            return (PollDTO) Cache.get(cacheKey);
        }
        
        PollDTO poll = pollService.getPollById(id, userId);
        Cache.put(cacheKey, poll);
        return poll;
    }
    
    public void invalidateCache() {
        Cache.clear();
    }
}
