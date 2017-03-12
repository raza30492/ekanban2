package com.example.ics.restcontroller;

import com.example.ics.assembler.CategoryAssembler;
import com.example.ics.assembler.SubCategoryAssembler;
import com.example.ics.entity.Category;
import com.example.ics.entity.SubCategory;
import com.example.ics.service.CategoryService;
import com.example.ics.service.SubCategoryService;
import com.example.ics.util.ApiUrls;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@RestController
@RequestMapping(value = ApiUrls.ROOT_URL_CATEGORIES)
public class CategoryRestController {

    private final Logger logger = LoggerFactory.getLogger(CategoryRestController.class);
    
    CategoryService categoryService;
    
    SubCategoryService subCategoryService;

    CategoryAssembler categoryAssembler;
    
    SubCategoryAssembler subCategoryAssembler;
    
    @Autowired
    public CategoryRestController(
            CategoryService categoryService,
            SubCategoryService subCategoryService,
            CategoryAssembler categoryAssembler,
            SubCategoryAssembler subCategoryAssembler
    ){
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.categoryAssembler = categoryAssembler;
        this.subCategoryAssembler = subCategoryAssembler;
    }
    
    @GetMapping
    public ResponseEntity<?> loadCategories(Pageable pageable, PagedResourcesAssembler assembler){
        logger.debug("getCategories()");
        Page<Category> page = categoryService.findAllByPage(pageable);
        return new ResponseEntity<>(assembler.toResource(page, categoryAssembler), HttpStatus.OK);
    }
    
    @GetMapping(value = ApiUrls.URL_CATEGORIES_CATEGORY)
    public ResponseEntity<?> loadCategory(@PathVariable("categoryId") Long categoryId){
        logger.debug("getCategory(): categoryId = {}",categoryId);
        Category category = categoryService.findOne(categoryId);
        if (category == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categoryAssembler.toResource(category), HttpStatus.OK);
    }
    
    @GetMapping(value = ApiUrls.URL_CATEGORIES_CATEGORY_SUBCATEGORIES)
    public ResponseEntity<?> loadCategorySubCategories(
            @PathVariable("categoryId") Long categoryId,
            Pageable pageable, 
            PagedResourcesAssembler assembler) {
        logger.debug("loadCategorySubCategories(): categoryId = {}",categoryId );
        Category category = categoryService.findOne(categoryId);
        if(category == null){
            return new ResponseEntity<>("Category with id = " + categoryId + " not found", HttpStatus.NOT_FOUND);
        }
        Page<SubCategory> page = subCategoryService.findPageByCategory(category, pageable);
        return new ResponseEntity<>(assembler.toResource(page, subCategoryAssembler), HttpStatus.OK);
    }
    
    @PostMapping(value = ApiUrls.URL_CATEGORIES_CATEGORY_SUBCATEGORIES)
    public ResponseEntity<?> createCategorySubCategory(
            @PathVariable("categoryId") Long categoryId,
            @Validated @RequestBody SubCategory subCategory
            ) {
        logger.debug("createCategorySubCategory(): categoryId= {} , subCategory = \n {}", categoryId, subCategory.toString());
        Category category = categoryService.findOne(categoryId);
        if(category == null){
            return new ResponseEntity<>("Category with id = " + categoryId + " not found", HttpStatus.NOT_FOUND);
        }
        subCategory.setCategory(category);
        subCategory = subCategoryService.save(subCategory);
        Link link = linkTo(methodOn(CategoryRestController.class).createCategorySubCategory(categoryId, subCategory)).slash(subCategory.getId()).withSelfRel();
        return ResponseEntity.created(URI.create(link.getHref())).build();
    }
   
    @PostMapping
    public ResponseEntity<Void> createCategory(@Validated @RequestBody Category category) {
        logger.debug("createCategory():\n {}", category.toString());
        category = categoryService.save(category);
        Link selfLink = linkTo(CategoryRestController.class).slash(category.getId()).withSelfRel();
        return ResponseEntity.created(URI.create(selfLink.getHref())).build();
    }
 
    @PutMapping(value = ApiUrls.URL_CATEGORIES_CATEGORY)
    public ResponseEntity<?> updateCategory(@PathVariable("categoryId") long categoryId,@Validated @RequestBody Category category) {
        logger.debug("updateCategory(): id = {} \n {}",categoryId,category);
        if (!categoryService.exists(categoryId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        category.setId(categoryId);  
        category = categoryService.update(category);
        return new ResponseEntity<>(categoryAssembler.toResource(category), HttpStatus.OK);
    }
  
    @DeleteMapping(value = ApiUrls.URL_CATEGORIES_CATEGORY)
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") long categoryId) {
        logger.debug("deleteCategory(): categoryId = {}",categoryId);
        if (!categoryService.exists(categoryId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        categoryService.delete(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}