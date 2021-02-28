package com.xuecheng.manage_course.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.dao.SysDicthinaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysDicthinaryService {

    @Autowired
    GridFSBucket gridFSBucket;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    SysDicthinaryRepository sysDicthinaryRepository;

    public SysDictionary getByType(String type) {
        return sysDicthinaryRepository.findByDType(type);
    }
}
