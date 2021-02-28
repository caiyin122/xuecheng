package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void testFindByExample(){
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        CmsPage cmsPage = new CmsPage();
        //cmsPage.setPageId("5a94d79cb00ffc3ab4bfa4f6");
        cmsPage.setPageAliase("轮播");

        ExampleMatcher matcher = ExampleMatcher.matching();
        //模糊匹配设置
        matcher = matcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage, matcher);

        Page<CmsPage> all = this.cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }


}
