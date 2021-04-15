package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CmsPageClient cmsPageClient;

    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

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
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
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

    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(optional.isPresent()){
            CourseMarket courseMarket = optional.get();
            return courseMarket;
        }
        ExceptionCast.cast(CommonCode.INVALID_PARAM);
        return null;
    }


    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket oldCourseMarket = this.getCourseMarketById(courseId);
        if(oldCourseMarket == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        oldCourseMarket.setCharge(courseMarket.getCharge());
        oldCourseMarket.setPrice(courseMarket.getPrice());
        oldCourseMarket.setPrice_old(courseMarket.getPrice_old());
        oldCourseMarket.setQq(courseMarket.getQq());
        oldCourseMarket.setValid(courseMarket.getValid());

        courseMarketRepository.save(oldCourseMarket);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 向课程服务添加课程和图片的相关关系
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic;
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);

        // 如果存在则更新图片地址
        if(optionalCoursePic.isPresent()){
            coursePic = optionalCoursePic.get();
        }else{
            coursePic = new CoursePic();
            coursePic.setCourseid(courseId);
        }
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePicById(String courseId) {
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if(optionalCoursePic.isPresent()){
            return optionalCoursePic.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult deleteCoursePicById(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView courseView(String id) {
        CourseView courseView = new CourseView();
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }

        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCoursePic(coursePicOptional.get());
        }

        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            courseView.setCourseMarket(courseMarketOptional.get());
        }

        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    // 课程预览功能
    public CoursePublishResult preview(String id) {
        CourseBase one = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        //页面id
        String pageId = cmsPage1.getPageId();
        //页面url
        String pageUrl = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }

        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    @Transactional
    public CoursePublishResult publish(String id) {
        //课程信息
        CourseBase one = this.findCourseBaseById(id);
        //发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(id);

        if(!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //更新课程状态
        CourseBase courseBase = this.saveCoursePubState(id);
        if(courseBase == null){
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //课程索引...
        //创建课程索引信息
        CoursePub coursePub = createCoursePub(id);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(id, coursePub);
        if(newCoursePub==null){
            //创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        //页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //保存CoursePub
    public CoursePub saveCoursePub(String id, CoursePub coursePub){
        if(StringUtils.isNotEmpty(id)){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }
        if(coursePubNew == null){
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //更新时间戳为最新时间
        coursePub.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePub.setPubTime(date);
        coursePubRepository.save(coursePub);
        return coursePub;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional == null){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);
        return coursePub;
    }

    //更新课程发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    //发布课程正式页面
    public CmsPostPageResult publish_page(String courseId){
        CourseBase one = findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);
        //发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }
}
