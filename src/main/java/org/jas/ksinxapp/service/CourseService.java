package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.mappers.CourseMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.CourseCategory;
import org.jas.ksinxapp.repo.CourseRatingRepo;
import org.jas.ksinxapp.repo.CourseRepo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepo courseRepo;
    private final CourseMapper courseMapper;
    private final MinIoStorageService minIoStorageService;
    private final CourseRatingRepo courseRatingRepo;


    @Transactional
    public CourseResponse createResponse(CourseCreateRequest request, MultipartFile image, MultipartFile video) {
        //convert dto to entity
        Course course = courseMapper.toEntity(request);
        if (request.category() == null) {
            course.setCategory(CourseCategory.OTHER);
        }

        //store the cover photo and the teaser video, if provided
        if (image != null && !image.isEmpty()) {
            course.setImageUrl(minIoStorageService.publicUpload(image));
        }
        if (video != null && !video.isEmpty()) {
            course.setVideoUrl(minIoStorageService.publicUpload(video));
        }

        //save to postgresql
        Course savedCourse = courseRepo.save(course);

        //convert back to dto and return
        return toEnrichedResponse(savedCourse);
    }

    @Transactional
    public List<CourseResponse> getAllResponse(int pageNo, int pageSize, String sortBy, String sortDir,
                                               CourseCategory category, boolean includeInactive) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        List<Course> courses;
        if (includeInactive) {
            //admin/teacher view — every course, optionally filtered by category
            courses = courseRepo.findAll(pageable)
                    .stream()
                    .filter(c -> category == null || category.equals(c.getCategory()))
                    .collect(Collectors.toList());
        } else if (category != null) {
            courses = courseRepo.findByIsActiveTrueAndCategory(category, pageable).getContent();
        } else {
            courses = courseRepo.findByIsActiveTrue(pageable).getContent();
        }

        return courses.stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "course",
            key = "#id"
    )
    @Transactional
    public CourseResponse findById(Long id){
        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));
        //inactive courses are still returned so the UI can show the "inactive" state;
        //enrolment is blocked separately in EnrollmentService
        return toEnrichedResponse(course);
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
            course.setImageUrl(minIoStorageService.publicUpload(image));
        }
        if (video != null && !video.isEmpty()) {
            course.setVideoUrl(minIoStorageService.publicUpload(video));
        }

        return toEnrichedResponse(course);
    }

    @CacheEvict(
            value = "course",
            key = "#id"
    )
    @Transactional
    public CourseResponse setActive(Long id, Boolean isActive){
        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        course.setIsActive(Boolean.TRUE.equals(isActive));
        courseRepo.save(course);

        return toEnrichedResponse(course);
    }

    private CourseResponse toEnrichedResponse(Course course) {
        Double avg = courseRatingRepo.findAverageByCourseId(course.getId());
        Long count = courseRatingRepo.countByCourseId(course.getId());
        int totalModules = course.getModules() != null ? course.getModules().size() : 0;
        double rounded = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getImageUrl(),
                course.getVideoUrl(),
                totalModules,
                course.getCategory(),
                course.getIsActive(),
                rounded,
                count
        );
    }
}
