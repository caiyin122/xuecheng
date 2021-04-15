package com.xuecheng.manage_course.client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "xc-service-manage-cms")
public interface CmsPageClient {
     // 根据页面ID查询页面信息， 远程调用cms请求信息
     @GetMapping("/cms/page/get/{id}")    // 用GetMapping标识远程调用的http方法类型
     public CmsPage findCmsPageById(@PathVariable("id") String id);

     // 添加页面 用于页面预览
     @PostMapping("/cms/page/save")
     public CmsPageResult  saveCmsPage(@RequestBody CmsPage cmsPage);

     //一键发布页面
     @PostMapping("/cms/page/postPageQuick")
     public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
