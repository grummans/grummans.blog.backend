package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagsRepository tagsRepository;
    private final TagMapper tagMapper;

    public List<TagDTO.TagSimpleDTO> getAllTags() {
        return tagsRepository.findAll().stream()
                .map(tagMapper::toTagSimpleDTO)
                .toList();
    }

    public TagDTO.TagSimpleDTO createTag(TagDTO.Req req) {
        Tags tags = tagMapper.toTag(req);
        Tags savedTag = tagsRepository.save(tags);
        return tagMapper.toTagSimpleDTO(savedTag);
    }
}
