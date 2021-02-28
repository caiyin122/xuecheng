package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public QueryResponseResult<CourseBase> findList(int page, int size, QueryPageRequest request){

        ExampleMatcher matcher = ExampleMatcher.matching();
        //模糊匹配设置
        matcher = matcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        CmsPage cmsPage = new CmsPage();

        if(StringUtils.isNotEmpty(request.getSiteId())){
            cmsPage.setSiteId(request.getSiteId());
        }

        if(StringUtils.isNotEmpty(request.getTemplateId())){
            cmsPage.setTemplateId(request.getTemplateId());
        }
        if(StringUtils.isNotEmpty(request.getPageName())){
            cmsPage.setPageAliase(request.getPageName());
        }
        //定义一个条件对象
        Example<CmsPage> example = Example.of(cmsPage, matcher);

        if(page <= 0){
            page = 1;
        }
        page = page -1;

        if(size <= 0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        //自定义条件的分页查询
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult queryResult = new QueryResult();
        // 数据列表
        queryResult.setList(all.getContent());
        // 数据总记录数
        queryResult.setTotal(all.getTotalElements());

        QueryResponseResult<CourseBase> responseResult = new QueryResponseResult<CourseBase>(CommonCode.SUCCESS, queryResult);
        return responseResult;
    }

    public CmsPageResult add(CmsPage cmsPage){
        // 校验页面名称 站点id 和webPath的唯一性

        // 根据页面名称 站点id 和webPath去查cmsPage集合，如果查到说明此页面已经存在 如果不存在则进行添加
        CmsPage cmsPage1 = cmsPageRepository.findCmsPaqgeByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(),
                cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            // 说明页面已经存在
            // 抛出异常 说明页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

            // 调用dao新增页面
            // pageId是一个主键 让其由MongoDB自动生成
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    /**
     *  根据id查询页面
     * @param id String
     * @return CmsPage
     */
    public CmsPage getById(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return  null;
    }

    /**
     *  页面更新方法
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id, CmsPage cmsPage){
        CmsPage one = this.getById(id);
        if(one != null){
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());

            //执行更新
            cmsPageRepository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS, one);
        }

        // 执行失败
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    public ResponseResult delete(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);

    }

    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> byId = cmsConfigRepository.findById(id);
        if(byId.isPresent()){
            CmsConfig cmsConfig = byId.get();
            return cmsConfig;
        }
        return null;
    }

    public String getPageHtml(String pageId){
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if(model == null){
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板
        String template = getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        // 执行静态化
        String html = generaterHtml(template, model);
        return html;
    }

    /**
     *  页面发布
     * @param pageId
     * @return
     */
    public ResponseResult post(String pageId) {
        // 页面静态化
        String pageHtml = this.getPageHtml(pageId);
        // 将静态化文件存储到gridFS中
        CmsPage cmsPage = this.save(pageId, pageHtml);
        // 向MQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *  静态化方法
     * @param template
     * @param model
     * @return
     */
    private String generaterHtml(String templateContent, Map model){
        // 创建一个配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        //向configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);

        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  获取数据模型
     * @param pageId
     * @return
     */
    private Map getModelByPageId(String pageId){
        // 取出页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        // 通过Template请求数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map map = forEntity.getBody();
        return map;
    }

    /**
     *  发送消息到MQ
     * @param pageId
     */
    private void sendPostPage(String pageId){
        // 取出页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 创建消息对象
        Map<String, String> msg = new HashMap<>();
        msg.put("pageId", pageId);
        // 转成json串
        String jsonString = JSON.toJSONString(msg);
        // 发送给MQ
        // 站点id
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, jsonString);

    }

    /**
     *  获取页面模板信息
     * @param pageId
     * @return
     */
    private String getTemplateByPageId(String pageId) {
        // 取出页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        // 获取页面的模板Id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        // 查询模板信息
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(templateId);
        if (byId.isPresent()) {
            CmsTemplate cmsTemplate = byId.get();
            // 获取模板文件Id
            String templateFileId = cmsTemplate.getTemplateFileId();

            //从GridFS中获取模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            assert gridFSFile != null;
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            // 从流中获取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
            return null;
    }


    /**
     *  保存HTML到GridFS
     * @param pageId
     * @param htmlContent
     * @return
     */
    private CmsPage save(String pageId, String htmlContent){
        // 先得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        ObjectId objectId = null;
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            // 将HTML内容保存到GridFS中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将html文件id更新到cmsPage
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
}
