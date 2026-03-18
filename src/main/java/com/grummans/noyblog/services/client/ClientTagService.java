package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientTagService {

    private final TagsRepository tagsRepository;
    private final PostTagsRepository postTagsRepository;
    private final TagMapper tagMapper;

    public List<TagDTO.TagSimpleDTO> getAllTags() {
        return tagsRepository.findAll().stream().map(tag -> {
            TagDTO.TagSimpleDTO dto = tagMapper.toTagSimpleDTO(tag);

            int postCount = postTagsRepository.countByTagId(tag.getId());
            dto.setPostCount(postCount);
            return dto;
        }).toList();
    }
}
