package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    SysDicthinaryRepository sysDicthinaryRepository;

    @Autowired
    CourseService courseService;

    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }

    @Test
    public void testTeachplanMapper(){
        TeachplanNode teachplanNode = teachplanMapper.selectList("4028e581617f945f01617f9dabc40000");
        System.out.println(teachplanNode);
    }

    @Test
    public void CategoryMapper(){
        CategoryNode categoryNode = categoryMapper.findList();
        System.out.println(categoryNode);
    }

    @Test
    public void testSysDictionary(){
        SysDictionary dictionary = sysDicthinaryRepository.findByDType("201");
        System.out.println(dictionary);
    }

    @Test
    public void testgetCourseBaseById(){
        CourseBase getCourseBaseById = courseService.getCourseBaseById("4028e581617f945f01617f9dabc40000");
        System.out.println(getCourseBaseById);
    }
}
