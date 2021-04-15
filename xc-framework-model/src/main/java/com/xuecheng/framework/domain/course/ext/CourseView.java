package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author cy
 * @Date 2021/4/4 16:07
 */
@Data
@NoArgsConstructor
@ToString
public class CourseView implements java.io.Serializable {
    private CourseBase courseBase;   // 基础课程信息
    private CoursePic coursePic;     // 课程图片信息
    private CourseMarket courseMarket;    // 课程营销信息
    private TeachplanNode teachplanNode;   // 教学计划
}
