package com.gamesplatform.zoo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.zoo.entity.ZooCareRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动物园照料记录数据访问接口。
 */
@Mapper
public interface ZooCareRecordMapper extends BaseMapper<ZooCareRecord> {
}
