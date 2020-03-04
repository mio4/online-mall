package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 使用通用Mapper配套的PageHelper辅助分页查询
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) throws LyException {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name","%" + key + "%").orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list =  brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(),list);
    }


    /**
     * 保存品牌
     * `@Transactional` 执行事务
     * @param brand
     * @param categories
     * @throws LyException
     */
    @Transactional
    public void saveBrand(Brand brand, List<Integer> categories) throws LyException {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //中间表处理——没有实体类
        for (Integer category : categories) {
            count = brandMapper.insertCategoryBrand(category,brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 通过主键ID查询品牌
     * @param id
     * @return
     * @throws LyException
     */
    public Brand queryById(Long id) throws LyException {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) throws LyException {
        List<Brand> brands = brandMapper.selectBrandIdAndNameByCid(cid);
        if(brands == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) throws LyException {
        List<Brand> brands = brandMapper.selectByIdList(ids);
//        printBrandList(brands);
//        printList(ids);
        if(brands == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    private void printBrandList(List<Brand> res){
        for (Brand brand : res){
            System.out.println(brand);
        }
    }

    private void printList(List<Long> ids){
        for(Long id : ids){
            System.out.println(id);
        }
    }
}
