package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.TagsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTagService {

    private static final String ERROR_TAG_NOT_FOUND = "Tag not found with id: ";

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

    public TagDTO.TagSimpleDTO createTag(TagDTO.Req req) {
        Tags tags = tagMapper.toTag(req);
        Tags savedTag = tagsRepository.save(tags);
        return tagMapper.toTagSimpleDTO(savedTag);
    }

    public TagDTO.TagSimpleDTO getDetailTag(int tagId) {
        Tags tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException(ERROR_TAG_NOT_FOUND + tagId));
        return tagMapper.toTagSimpleDTO(tag);
    }

    public TagDTO.TagSimpleDTO updateTag(int tagId, TagDTO.Req req) {
        Tags tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException(ERROR_TAG_NOT_FOUND + tagId));
        tag.setName(req.getName());
        tag.setSlug(req.getSlug());
        Tags updatedTag = tagsRepository.save(tag);
        return tagMapper.toTagSimpleDTO(updatedTag);
    }

    public void deleteTag(int tagId) {
        Tags tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException(ERROR_TAG_NOT_FOUND + tagId));
        tagsRepository.delete(tag);
    }
}
