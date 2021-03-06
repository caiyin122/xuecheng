package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage, String> {

     // 根据页面名称查询
    CmsPage findByPageName(String pageName);

    // 根据页面名称 站点id 和webPath查询
    CmsPage findCmsPaqgeByPageNameAndSiteIdAndPageWebPath(String pageName, String siteId, String pageWebPath);

}
