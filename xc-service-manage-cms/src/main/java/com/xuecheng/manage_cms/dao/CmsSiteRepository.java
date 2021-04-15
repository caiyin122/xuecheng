package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author cy
 * @date 2021/4/11 15:36
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
