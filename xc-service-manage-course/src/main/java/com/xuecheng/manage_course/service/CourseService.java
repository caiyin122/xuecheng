package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CourseMapper;
import com.xuecheng.manage_course.dao.TeachplanMapper;
import com.xuecheng.manage_course.dao.TeachplanRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    // 课程计划的查询
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    /**
     *  添加课程计划
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        String courseid = teachplan.getCourseid();
        // 页面传入的parentId
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            // 取出该课程的根节点
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        // 父节点的级别
        String grade = parentNode.getGrade();
        //  新的节点
        Teachplan teachplanNew = new Teachplan();
        // 将teachplan的信息拷贝到新的结点中
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        // 查询课程的根节点
        List<Teachplan> teachplans = teachplanRepository.findTeachplanByCourseidAndParentid(courseId, "0");
        if(teachplans == null || teachplans.size() <= 0){
            // 查询不到 需要自动的进行添加
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            // 表示未发布
            teachplan.setStatus("0");
            teachplan.setCourseid(courseId);
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        // 返回根节点id
        return teachplans.get(0).getId();
    }

    /**
     *  查询我的课程列表
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest) {
        PageHelper.startPage(page, size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        if(courseListPage.isEmpty()){
            return null;
        }
        List<CourseInfo> courseInfos = courseListPage.getResult();
        QueryResult<CourseInfo> result = new QueryResult<>();
        result.setList(courseInfos);
        return new QueryResponseResult<>(CommonCode.SUCCESS, result);
    }

    public ResponseResult addCourseBase(CourseBase courseBase) {
        if(courseBase ==null || StringUtils.isEmpty(courseBase.getName())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }

        return null;
    }

    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase) {
        CourseBase oldcourseBase = this.getCourseBaseById(courseId);
        if(oldcourseBase == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //courseBase.setId(oldcourseBase.getId());
        oldcourseBase.setName(courseBase.getName());
        oldcourseBase.setDescription(courseBase.getDescription());
        oldcourseBase.setGrade(courseBase.getGrade());
        oldcourseBase.setSt(courseBase.getSt());
        oldcourseBase.setMt(courseBase.getMt());
        oldcourseBase.setStudymodel(courseBase.getStudymodel());
        oldcourseBase.setTeachmode(courseBase.getTeachmode());
        oldcourseBase.setUsers(courseBase.getUsers());
        courseBaseRepository.save(oldcourseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
