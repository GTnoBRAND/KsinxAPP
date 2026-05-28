package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.mappers.CourseMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.repo.CourseRepo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepo courseRepo;
    private final CourseMapper courseMapper;
    private final FileStorageService fileStorageService;
    private final RedisTemplate<String, Course> redisTemplate;
    private final String CACHE_KEY_PREFIX = "product: ";
    private final long MANUAL_CACHE_TTL = 5;


    @Transactional
    public CourseResponse createResponse(CourseCreateRequest request, MultipartFile image, MultipartFile video) {
        //convert dto to entity
        Course course = courseMapper.toEntity(request);

        //store the cover photo and the teaser video, if provided
        if (image != null && !image.isEmpty()) {
            course.setImageUrl(fileStorageService.storeFile(image));
        }
        if (video != null && !video.isEmpty()) {
            course.setVideoUrl(fileStorageService.storeFile(video));
        }

        //save to postgresql
        Course savedCourse = courseRepo.save(course);



        //convert back to dto and return
        return courseMapper.toResponse(savedCourse);
    }

    @Transactional
    public List<CourseResponse> getAllResponse(int pageNo, int pageSize, String sortBy, String sortDir) {

        //handle sorting
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ?Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        //handle pagination
        Pageable pageable = PageRequest.of(pageNo -1, pageSize, sort);

//        //handle dynamic filtering with specification
        return courseRepo.findByIsActiveTrue(pageable)
                .stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Cacheable(
            value = "courseFromDb",
            key = "#id"
    )
    @Transactional
    public CourseResponse findById(Long id){

        var cacheKey = CACHE_KEY_PREFIX + id;

//        Course courseFromCache = redisTemplate.opsForValue()
//                .get(cacheKey);
//        if(courseFromCache != null){
//            if(!courseFromCache.getIsActive()){
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
//            }
//            log.info("Course is found in cache: id{}", id);
//            return courseMapper.toResponseDto(courseFromCache);
//        }


        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));
            if(!course.getIsActive()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found from DB");
            }
//            log.info("Course is found from DB");
//            redisTemplate.opsForValue()
//                    .set(cacheKey, courseFromDb, MANUAL_CACHE_TTL, TimeUnit.MINUTES);
        return courseMapper.toResponse(course);
    }
    @CacheEvict(
            value = "course",
            key = "#id"
    )
    @Transactional
    public CourseResponse updateResponse(Long id, CourseCreateRequest request, MultipartFile image, MultipartFile video) {

        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));

        courseMapper.updateEntityFromDto(request, course);

        //replace media only when a new file is uploaded; keep existing otherwise
        if (image != null && !image.isEmpty()) {
            course.setImageUrl(fileStorageService.storeFile(image));
        }
        if (video != null && !video.isEmpty()) {
            course.setVideoUrl(fileStorageService.storeFile(video));
        }

        //invalidate the cache
//        var cacheKey = CACHE_KEY_PREFIX + id;
//        redisTemplate.delete(cacheKey);
//        log.info("Cache invalidated for course: id{}", id);

        return courseMapper.toResponseDto(course);
    }
    @CacheEvict(
            value = "course",
            key = "#id"
    )
    @Transactional
    public CourseResponse deleteById(Long id, Boolean isActive){
        Course course = courseRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Course not found"));


        if(Boolean.TRUE.equals(course.getIsActive())){  //null safe check
            course.setIsActive(false);
            courseRepo.save(course); //actually persist the changes
        }

        //invalidate cache
//        var cacheKey = CACHE_KEY_PREFIX+ id;
//        redisTemplate.delete(cacheKey);
//        log.info("Cache invalidated for deleted course: id{}", id);

        return courseMapper.toResponseDto(course);
    }
}
