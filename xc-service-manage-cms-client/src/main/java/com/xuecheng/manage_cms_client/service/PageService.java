package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PageService.class);

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     *  保存HTML文件到服务器的物理路径
     * @param pageId
     */
    public void savePageToServerPath(String pageId){
        // 根据pageId查询CmsPage
        CmsPage cmsPage = this.findCmsPageById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 得到HTML的文件id，是从CmsPage中的htmlFileId字段获得
        String htmlFileId = cmsPage.getHtmlFileId();
        // 从GridFS中查询HTML文件
        InputStream inputstream = this.getFileById(htmlFileId);
        if(inputstream == null){
            LOGGER.error("getFileById get Inputstream is null, htmlFileId:{}", htmlFileId);
            return;
        }
        // 得到站点id
        String siteId = cmsPage.getSiteId();
        // 得到站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        // 得到站点的物理路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        // 得到页面的物理路径
        String pagePath = sitePhysicalPath + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        // 将HTML文件保存到服务器的物理路径上
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputstream, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private InputStream getFileById(String filedId){
        // 文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(filedId)));
        // 打开一个下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        // 定义一个GridFSResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  根据页面id查询页面信息
     * @param pageId
     * @return
     */
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);

        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     *  根据站点id查询站点信息
     * @param siteId
     * @return
     */
    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);

        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
