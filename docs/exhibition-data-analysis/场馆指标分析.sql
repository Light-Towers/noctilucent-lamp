-- ## 客户相关指标
-- 客户历史总合同金额
SELECT 
    t1.cutomer_id, 
    --以此为准：取客户主表中的中文名称（即最新的标准名称）
    t2.customer_cn_name AS '客户名称', 
    SUM(t1.contract_amount) AS '总合同金额'
FROM 
    book_vb_contract_manage t1
LEFT JOIN 
    book_vb_customer_main t2 ON t1.cutomer_id = t2.id
WHERE 
    -- 确保只统计已审核的合同
    t1.contract_status = 'APPROVED' 
    AND t1.contract_amount > 0 
GROUP BY 
    t1.cutomer_id, 
    t2.customer_cn_name;


-- 客户活跃度分析：客户展会举办频率与规模
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    COUNT(DISTINCT e.id) AS '举办展会总数',
    COUNT(DISTINCT CASE WHEN e.exhibition_main_type = 'SELF_EXHIBITION' THEN e.id END) AS '自办展数量',
    COUNT(DISTINCT CASE WHEN e.exhibition_main_type = 'GUEST_EXHIBITION' THEN e.id END) AS '客展数量',
    COUNT(DISTINCT es.space_id) AS '使用场地总数',
    AVG(e.reserve_area) AS '平均预定面积',
    MAX(e.expect_viewer) AS '最大预期人数',
    MIN(e.exhibition_start_time) AS '首次办展时间',
    MAX(e.exhibition_start_time) AS '最近办展时间'
FROM book_vb_customer_main c
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1 AND e.is_deleted = 0
LEFT JOIN book_vb_exhibition_space es ON e.id = es.exhibition_id AND es.is_enabled = 1
WHERE c.is_deleted = 0 AND c.is_enabled = 1
GROUP BY c.id, c.customer_cn_name
HAVING COUNT(DISTINCT e.id) > 0;


-- 客户价值分层分析（最近一次展会、展会频率、合同金额）
WITH customer_metrics AS (
    SELECT 
        c.id,
        c.customer_cn_name AS '客户名称',
        -- Recency: 最近一次展会距今天数
        DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) AS recency_days,
        -- Frequency: 近3年展会数量
        COUNT(DISTINCT CASE 
            WHEN e.exhibition_end_time >= DATE_SUB(CURDATE(), INTERVAL 3 YEAR) 
            THEN e.id 
        END) AS frequency_3y,
        -- Monetary: 近3年合同总金额
        SUM(CASE 
            WHEN cm.signing_time >= DATE_SUB(CURDATE(), INTERVAL 3 YEAR)
            AND cm.contract_status = 'APPROVED'
            THEN cm.contract_amount ELSE 0 
        END) AS monetary_3y
    FROM book_vb_customer_main c
    LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
    LEFT JOIN book_vb_contract_manage cm ON c.id = cm.cutomer_id AND cm.contract_status = 'APPROVED'
    WHERE c.is_deleted = 0 AND c.is_enabled = 1
    GROUP BY c.id, c.customer_cn_name
),
rfm_scores AS (
    SELECT *,
        -- 计算RFM得分（1-5分）
        NTILE(5) OVER (ORDER BY recency_days DESC) AS r_score,
        NTILE(5) OVER (ORDER BY frequency_3y) AS f_score,
        NTILE(5) OVER (ORDER BY monetary_3y) AS m_score
    FROM customer_metrics
)
SELECT *,
    CASE 
        WHEN r_score >= 4 AND f_score >= 4 AND m_score >= 4 THEN '高价值客户'
        WHEN r_score >= 3 AND f_score >= 3 AND m_score >= 3 THEN '中价值客户'
        WHEN r_score <= 2 OR f_score <= 2 OR m_score <= 2 THEN '低价值客户'
        ELSE '一般客户'
    END AS '客户价值分层',
    (r_score + f_score + m_score) AS 'RFM总分'
FROM rfm_scores
ORDER BY (r_score + f_score + m_score) DESC;


-- 客户生命周期与转化分析
SELECT 
    -- 时间维度（按年）
    YEAR(c.create_time) AS '客户获取年份',
    -- 新增客户数
    COUNT(DISTINCT c.id) AS '新增客户数',
    -- 转化为商机
    COUNT(DISTINCT bo.id) AS '产生商机客户数',
    COUNT(DISTINCT CASE WHEN bo.id IS NOT NULL THEN c.id END) * 100.0 / COUNT(DISTINCT c.id) AS '商机转化率',
    -- 转化为合同
    COUNT(DISTINCT cm.id) AS '产生合同客户数',
    COUNT(DISTINCT CASE WHEN cm.id IS NOT NULL THEN c.id END) * 100.0 / COUNT(DISTINCT c.id) AS '合同转化率',
    -- 转化为展会
    COUNT(DISTINCT e.id) AS '举办展会客户数',
    COUNT(DISTINCT CASE WHEN e.id IS NOT NULL THEN c.id END) * 100.0 / COUNT(DISTINCT c.id) AS '展会转化率',
    -- 留存情况（次年仍有活动）
    COUNT(DISTINCT CASE 
        WHEN EXISTS (
            SELECT 1 FROM book_vb_exhibition e2 
            WHERE e2.customer_id = c.id 
            AND YEAR(e2.exhibition_start_time) = YEAR(c.create_time) + 1
        ) THEN c.id 
    END) AS '次年留存客户数'
FROM book_vb_customer_main c
LEFT JOIN book_vb_business_opportunity bo ON c.id = bo.customer_id AND bo.is_enabled = 1
LEFT JOIN book_vb_contract_manage cm ON c.id = cm.cutomer_id AND cm.contract_status = 'APPROVED'
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
WHERE c.is_deleted = 0 AND c.is_enabled = 1
    AND c.create_time >= '2020-01-01'
GROUP BY YEAR(c.create_time)
ORDER BY YEAR(c.create_time);


-- 客户附加服务使用分析
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    -- 材料申报情况
    COUNT(DISTINCT em.id) AS '材料申报次数',
    -- 报馆情况
    COUNT(DISTINCT rs.id) AS '报馆申请次数',
    COUNT(DISTINCT CASE WHEN rs.report_space_status = 'APPROVED' THEN rs.id END) AS '报馆通过次数',
    -- 主场服务使用
    COUNT(DISTINCT hs.id) AS '主场服务使用次数',
    -- 维修报修情况
    COUNT(DISTINCT brr.id) AS '维修报修次数'
FROM book_vb_customer_main c
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
LEFT JOIN book_vb_exhibition_material em ON e.id = em.exhibition_id AND em.is_enabled = 1
LEFT JOIN builder_bd_report_space rs ON e.id = rs.exhibition_id AND rs.is_enabled = 1
LEFT JOIN hc_hc_home_service_item hs ON e.id = hs.exhibition_manage_id AND hs.is_enabled = 1
LEFT JOIN hc_hc_builder_repair_report brr ON e.id = brr.exhibition_manage_id AND brr.is_enabled = 1
WHERE c.is_deleted = 0 AND c.is_enabled = 1
GROUP BY c.id, c.customer_cn_name;


-- 客户场地类型偏好分析
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    c.customer_industry AS '行业',
    -- 使用场地类型分布
    COUNT(DISTINCT es.space_id) AS '使用场地总数',
    COUNT(DISTINCT CASE WHEN sc.space_type = 'EXHIBITION_HALL' THEN es.space_id END) AS '展厅数量',
    COUNT(DISTINCT CASE WHEN sc.space_type = 'CONFERENCE_ROOM' THEN es.space_id END) AS '会议室数量',
    COUNT(DISTINCT CASE WHEN sc.space_type = 'ADVERTISING_SPACE' THEN es.space_id END) AS '广告位数量',
    -- 面积偏好
    AVG(s.price_area) AS '平均使用面积',
    MAX(s.price_area) AS '最大使用面积',
    MIN(s.price_area) AS '最小使用面积',
    -- 时间段偏好
    COUNT(DISTINCT CASE 
        WHEN HOUR(e.exhibition_start_time) BETWEEN 9 AND 17 
        THEN e.id END) AS '日间展会数量',
    COUNT(DISTINCT CASE 
        WHEN HOUR(e.exhibition_start_time) BETWEEN 18 AND 22 
        THEN e.id END) AS '晚间活动数量'
FROM book_vb_customer_main c
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
LEFT JOIN book_vb_exhibition_space es ON e.id = es.exhibition_id AND es.is_enabled = 1
LEFT JOIN book_vb_space s ON es.space_id = s.id AND s.is_enabled = 1
LEFT JOIN book_vb_space_category sc ON s.space_category_id = sc.id
WHERE c.is_deleted = 0 AND c.is_enabled = 1
GROUP BY c.id, c.customer_cn_name, c.customer_industry;


-- 客户履约能力分析
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    c.customer_level AS '客户等级',
    -- 合同履约情况
    COUNT(DISTINCT cm.id) AS '总合同数',
    COUNT(DISTINCT CASE WHEN cm.contract_pay_state = '已付清' THEN cm.id END) AS '已付清合同数',
    COUNT(DISTINCT CASE WHEN DATEDIFF(CURDATE(), cm.signing_time) > 90 
        AND cm.contract_pay_state != '已付清' THEN cm.id END) AS '逾期合同数',
    -- 付款及时性
    AVG(DATEDIFF(p.actual_payment_date, p.due_date)) AS '平均逾期天数',
    -- 保证金缴纳情况
    COUNT(DISTINCT CASE WHEN eb.earnest_money_pay_status = 'HAS_PAY' THEN eb.id END) AS '保证金已缴次数',
    COUNT(DISTINCT CASE WHEN eb.earnest_money_pay_status = 'NOT_PAY' THEN eb.id END) AS '保证金未缴次数',
    -- 黑名单记录
    MAX(CASE WHEN bl.id IS NOT NULL THEN 1 ELSE 0 END) AS '是否在黑名单'
FROM book_vb_customer_main c
LEFT JOIN book_vb_contract_manage cm ON c.id = cm.cutomer_id AND cm.contract_status = 'APPROVED'
LEFT JOIN book_vb_collect_record cr ON cm.id = cr.payment_plan_id
LEFT JOIN builder_bd_exhibitor eb ON c.id = eb.external_exhibitor_id
LEFT JOIN cert_cert_blacklist bl ON c.id = bl.company_id
WHERE c.is_deleted = 0 AND c.is_enabled = 1
GROUP BY c.id, c.customer_cn_name, c.customer_level;


-- 客户问题频次分析
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    -- 报修问题
    COUNT(DISTINCT brr.id) AS '维修报修次数',
    COUNT(DISTINCT CASE WHEN brr.repair_status = 'COMPLETE' THEN brr.id END) AS '已解决报修',
    COUNT(DISTINCT CASE WHEN brr.is_need_compensate = 1 THEN brr.id END) AS '需赔偿报修',
    -- 报馆审核问题
    COUNT(DISTINCT CASE WHEN rs.drawing_audit_status = 'NOT_APPROVED' THEN rs.id END) AS '图纸审核不通过次数',
    COUNT(DISTINCT CASE WHEN rs.service_audit_status = 'NOT_APPROVED' THEN rs.id END) AS '服务审核不通过次数',
    -- 违规记录
    COUNT(DISTINCT CASE WHEN fl.record_type = 'ILLEGAL' THEN fl.id END) AS '违规通行记录'
FROM book_vb_customer_main c
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
LEFT JOIN hc_hc_builder_repair_report brr ON e.id = brr.exhibition_manage_id
LEFT JOIN builder_bd_report_space rs ON e.id = rs.exhibition_id
LEFT JOIN cert_cert_prople_record fl ON e.id = fl.exhibition_id
WHERE c.is_deleted = 0 AND c.is_enabled = 1
GROUP BY c.id, c.customer_cn_name;


-- 潜在流失客户识别
SELECT 
    c.id AS '客户ID',
    c.customer_cn_name AS '客户名称',
    c.customer_level AS '客户等级',
    MAX(e.exhibition_end_time) AS '最近展会时间',
    DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) AS '距上次展会天数',
    COUNT(DISTINCT e.id) AS '历史展会总数',
    AVG(DATEDIFF(e.exhibition_end_time, e.exhibition_start_time)) AS '平均展会时长',
    -- 最近跟进情况
    MAX(fr.follow_date) AS '最近跟进时间',
    DATEDIFF(CURDATE(), MAX(fr.follow_date)) AS '距上次跟进天数',
    COUNT(DISTINCT fr.id) AS '总跟进次数',
    -- 状态评估
    CASE 
        WHEN DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) > 365 THEN '已流失'
        WHEN DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) > 180 THEN '高风险'
        WHEN DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) > 90 THEN '中风险'
        ELSE '低风险'
    END AS '流失风险等级'
FROM book_vb_customer_main c
LEFT JOIN book_vb_exhibition e ON c.id = e.customer_id AND e.is_enabled = 1
LEFT JOIN book_vb_follow_record fr ON c.id = fr.customer_id AND fr.is_enabled = 1
WHERE c.is_deleted = 0 
    AND c.is_enabled = 1
    AND c.state = 0  -- 正常客户
    AND (e.exhibition_end_time IS NOT NULL OR fr.follow_date IS NOT NULL)
GROUP BY c.id, c.customer_cn_name, c.customer_level
HAVING MAX(e.exhibition_end_time) IS NOT NULL
ORDER BY DATEDIFF(CURDATE(), MAX(e.exhibition_end_time)) DESC;



-- 客户综合画像
SELECT 
    c.*,
    -- 业务数据
    e_stats.exhibition_count,
    e_stats.total_reserve_area,
    e_stats.avg_expect_viewer,
    contract_stats.total_contract_amount,
    contract_stats.avg_contract_amount,
    follow_stats.last_follow_date,
    follow_stats.follow_count_30d,
    -- 风险数据
    risk_stats.overdue_contracts,
    risk_stats.blacklist_flag,
    -- 行为数据
    behavior_stats.avg_booth_area,
    behavior_stats.preferred_space_type
FROM book_vb_customer_main c
LEFT JOIN (
    SELECT 
        customer_id,
        COUNT(*) AS exhibition_count,
        SUM(reserve_area) AS total_reserve_area,
        AVG(expect_viewer) AS avg_expect_viewer
    FROM book_vb_exhibition
    WHERE is_enabled = 1 AND is_deleted = 0
    GROUP BY customer_id
) e_stats ON c.id = e_stats.customer_id
LEFT JOIN (
    SELECT 
        cutomer_id,
        COUNT(*) AS contract_count,
        SUM(contract_amount) AS total_contract_amount,
        AVG(contract_amount) AS avg_contract_amount
    FROM book_vb_contract_manage
    WHERE contract_status = 'APPROVED' AND is_enabled = 1
    GROUP BY cutomer_id
) contract_stats ON c.id = contract_stats.cutomer_id
LEFT JOIN (
    SELECT 
        customer_id,
        MAX(follow_date) AS last_follow_date,
        COUNT(CASE WHEN follow_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) AS follow_count_30d
    FROM book_vb_follow_record
    WHERE is_enabled = 1
    GROUP BY customer_id
) follow_stats ON c.id = follow_stats.customer_id
LEFT JOIN (
    SELECT 
        cutomer_id,
        COUNT(CASE WHEN contract_pay_state != '已付清' 
            AND DATEDIFF(CURDATE(), signing_time) > 90 THEN 1 END) AS overdue_contracts,
        MAX(CASE WHEN customer_credit_rating = 'C' THEN 1 ELSE 0 END) AS blacklist_flag
    FROM book_vb_contract_manage
    GROUP BY cutomer_id
) risk_stats ON c.id = risk_stats.cutomer_id
LEFT JOIN (
    SELECT 
        e.customer_id,
        AVG(rs.site_area) AS avg_booth_area,
        GROUP_CONCAT(DISTINCT sc.space_type ORDER BY COUNT(*) DESC LIMIT 1) AS preferred_space_type
    FROM book_vb_exhibition e
    JOIN builder_bd_report_site rs ON e.id = rs.exhibition_manage_id
    JOIN book_vb_space s ON rs.space_code = s.space_code
    JOIN book_vb_space_category sc ON s.space_category_id = sc.id
    GROUP BY e.customer_id
) behavior_stats ON c.id = behavior_stats.customer_id
WHERE c.is_deleted = 0 AND c.is_enabled = 1;



-- ## 场地相关指标
-- 指标：过去12个月场地时间利用率
-- 价值：评估场地资源使用效率，识别闲置资源
SELECT 
    s.id AS '场地ID',
    s.space_name AS '场地名称',
    s.space_category_id,
    sc.category_name AS '场地类型',
    s.price_area AS '计价面积(㎡)',
    -- 已预订天数（展会+会议）
    COUNT(DISTINCT e.id) AS '预订次数',
    SUM(
        CASE 
            WHEN e.arrangement_start_time IS NOT NULL AND e.dismantling_end_time IS NOT NULL
            THEN TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time) + 1
            ELSE 0
        END
    ) AS '已预订总天数',
    -- 利用率计算(过去365天)
    ROUND(
        (SUM(
            CASE 
                WHEN e.arrangement_start_time IS NOT NULL AND e.dismantling_end_time IS NOT NULL
                THEN TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time) + 1
                ELSE 0
            END
        ) / 365) * 100, 2
    ) AS '时间利用率(%)',
    -- 收入贡献
    SUM(COALESCE(cm.space_amount, 0)) AS '场地收入(元)',
    ROUND(
        SUM(COALESCE(cm.space_amount, 0)) / s.price_area, 2
    ) AS '单位面积收入(元/㎡)'
FROM book_vb_space s
LEFT JOIN book_vb_space_category sc ON s.space_category_id = sc.id
LEFT JOIN book_vb_exhibition_space es ON s.id = es.space_id AND es.is_enabled = 1
LEFT JOIN book_vb_exhibition e ON es.exhibition_id = e.id 
    AND e.is_enabled = 1 
    AND e.is_deleted = 0
    AND e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
LEFT JOIN book_vb_contract_manage cm ON e.id = cm.exhibition_id 
    AND cm.contract_status = 'APPROVED'
    AND cm.is_enabled = 1
WHERE s.is_deleted = 0 AND s.is_enabled = 1
GROUP BY s.id, s.space_name, s.space_category_id, sc.category_name, s.price_area
ORDER BY ROUND((SUM(
    CASE 
        WHEN e.arrangement_start_time IS NOT NULL AND e.dismantling_end_time IS NOT NULL
        THEN TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time) + 1
        ELSE 0
    END
) / 365) * 100, 2) DESC;



-- 指标：场地维护成本与收益比
-- 价值：评估场地运营效率，优化资源配置
SELECT 
    s.id AS '场地ID',
    s.space_name AS '场地名称',
    sc.category_name AS '场地类型',
    s.price_area AS '面积(㎡)',
    s.build_area AS '建筑面积(㎡)',
    s.location AS '位置',
    -- 收入指标
    SUM(COALESCE(cm.space_amount, 0)) AS '场地费收入(元)',
    SUM(COALESCE(cm.space_bond, 0)) AS '保证金收入(元)',
    SUM(COALESCE(cm.service_bond, 0)) AS '服务预存款(元)',
    SUM(COALESCE(cm.space_amount, 0) + COALESCE(cm.space_bond, 0) + COALESCE(cm.service_bond, 0)) AS '总收入(元)',
    -- 维修与问题成本
    COUNT(DISTINCT brr.id) AS '维修报修次数',
    SUM(COALESCE(brr.compensate_venue_amount, 0)) AS '维修赔偿金额(元)',
    COUNT(DISTINCT CASE WHEN brr.is_need_compensate = 1 THEN brr.id END) AS '需赔偿次数',
    -- 报馆问题
    COUNT(DISTINCT rs.id) AS '报馆申请次数',
    COUNT(DISTINCT CASE WHEN rs.drawing_audit_status = 'NOT_APPROVED' THEN rs.id END) AS '图纸审核失败次数',
    COUNT(DISTINCT CASE WHEN rs.service_audit_status = 'NOT_APPROVED' THEN rs.id END) AS '服务审核失败次数',
    -- 场地锁定状态
    CASE WHEN s.is_lock = 1 THEN '已锁定' ELSE '可用' END AS '锁定状态',
    s.discount_rate AS '折扣比例(%)',
    -- 运营效率指标
    ROUND(
        CASE 
            WHEN SUM(COALESCE(cm.space_amount, 0)) > 0 
            THEN (COUNT(DISTINCT brr.id) * 100.0) / (COUNT(DISTINCT cm.id) + 1)
            ELSE 0
        END, 2
    ) AS '每合同维修率(%)',
    ROUND(
        CASE 
            WHEN SUM(COALESCE(cm.space_amount, 0)) > 0 
            THEN SUM(COALESCE(brr.compensate_venue_amount, 0)) * 100.0 / SUM(COALESCE(cm.space_amount, 0))
            ELSE 0
        END, 2
    ) AS '维修成本占收入比(%)'
FROM book_vb_space s
LEFT JOIN book_vb_space_category sc ON s.space_category_id = sc.id
LEFT JOIN book_vb_exhibition_space es ON s.id = es.space_id AND es.is_enabled = 1
LEFT JOIN book_vb_exhibition e ON es.exhibition_id = e.id AND e.is_enabled = 1
LEFT JOIN book_vb_contract_manage cm ON e.id = cm.exhibition_id 
    AND cm.contract_status = 'APPROVED'
    AND cm.is_enabled = 1
LEFT JOIN hc_hc_builder_repair_report brr ON e.id = brr.exhibition_manage_id 
    AND brr.is_enabled = 1
    AND brr.space_code = s.space_code
LEFT JOIN builder_bd_report_space rs ON e.id = rs.exhibition_id 
    AND rs.is_enabled = 1
    AND rs.space_code = s.space_code
WHERE s.is_deleted = 0 AND s.is_enabled = 1
    AND cm.signing_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY s.id, s.space_name, sc.category_name, s.price_area, s.build_area, s.location, s.is_lock, s.discount_rate
ORDER BY SUM(COALESCE(cm.space_amount, 0)) DESC;




-- 指标：场地预订频率与时间分布
-- 价值：了解客户偏好，优化排期策略
SELECT 
    s.id AS '场地ID',
    s.space_name AS '场地名称',
    sc.category_name AS '场地类型',
    -- 预订频率
    COUNT(DISTINCT e.id) AS '总预订次数',
    COUNT(DISTINCT CASE 
        WHEN e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) 
        THEN e.id 
    END) AS '近3月预订次数',
    COUNT(DISTINCT CASE 
        WHEN e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) 
        THEN e.id 
    END) AS '近12月预订次数',
    -- 时间分布
    COUNT(DISTINCT CASE 
        WHEN MONTH(e.arrangement_start_time) IN (3,4,5) 
        THEN e.id 
    END) AS '春季预订(3-5月)',
    COUNT(DISTINCT CASE 
        WHEN MONTH(e.arrangement_start_time) IN (6,7,8) 
        THEN e.id 
    END) AS '夏季预订(6-8月)',
    COUNT(DISTINCT CASE 
        WHEN MONTH(e.arrangement_start_time) IN (9,10,11) 
        THEN e.id 
    END) AS '秋季预订(9-11月)',
    COUNT(DISTINCT CASE 
        WHEN MONTH(e.arrangement_start_time) IN (12,1,2) 
        THEN e.id 
    END) AS '冬季预订(12-2月)',
    -- 活动类型分布
    COUNT(DISTINCT CASE 
        WHEN e.exhibition_type = 'EXHIBITION' 
        THEN e.id 
    END) AS '展会数量',
    COUNT(DISTINCT CASE 
        WHEN e.exhibition_type = 'MEETING' 
        THEN e.id 
    END) AS '会议数量',
    COUNT(DISTINCT CASE 
        WHEN e.exhibition_type = 'FORUM' 
        THEN e.id 
    END) AS '论坛数量',
    -- 平均预订时长
    ROUND(AVG(
        TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time)
    ), 1) AS '平均使用天数',
    -- 重复预订率
    COUNT(DISTINCT CASE 
        WHEN EXISTS (
            SELECT 1 FROM book_vb_exhibition e2 
            JOIN book_vb_exhibition_space es2 ON e2.id = es2.exhibition_id
            WHERE es2.space_id = s.id 
                AND e2.customer_id = e.customer_id 
                AND e2.id != e.id
        ) 
        THEN e.customer_id 
    END) AS '重复使用客户数',
    ROUND(
        COUNT(DISTINCT CASE 
            WHEN EXISTS (
                SELECT 1 FROM book_vb_exhibition e2 
                JOIN book_vb_exhibition_space es2 ON e2.id = es2.exhibition_id
                WHERE es2.space_id = s.id 
                    AND e2.customer_id = e.customer_id 
                    AND e2.id != e.id
            ) 
            THEN e.customer_id 
        END) * 100.0 / NULLIF(COUNT(DISTINCT e.customer_id), 0), 2
    ) AS '客户复购率(%)'
FROM book_vb_space s
LEFT JOIN book_vb_space_category sc ON s.space_category_id = sc.id
LEFT JOIN book_vb_exhibition_space es ON s.id = es.space_id AND es.is_enabled = 1
LEFT JOIN book_vb_exhibition e ON es.exhibition_id = e.id 
    AND e.is_enabled = 1 
    AND e.is_deleted = 0
WHERE s.is_deleted = 0 AND s.is_enabled = 1
GROUP BY s.id, s.space_name, sc.category_name
ORDER BY COUNT(DISTINCT e.id) DESC;


-- ## 展会相关指标
-- 指标：展会总收入、成本、利润分析
-- 价值：评估展会盈利能力，识别高收益展会类型
SELECT 
--     e.id AS '展会ID',
    e.exhibition_name AS '展会名称',
--     e.exhibition_no AS '展会编号',
--     e.exhibition_type AS '展会类型',
--     e.exhibition_main_type AS '主办类型',
--     e.exhibition_nature AS '展会性质',
    c.customer_cn_name AS '主办客户', 
    -- 收入构成
    COALESCE(SUM(cm.contract_amount), 0) AS '合同总金额',
    COALESCE(SUM(cm.space_amount), 0) AS '场地费收入',
    COALESCE(SUM(cm.space_bond + cm.service_bond), 0) AS '保证金收入',
    -- 成本估算（根据场地面积和天数估算）
    ROUND(
        (COALESCE(e.reserve_area, 0) * 
         TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time) * 5), 2
    ) AS '预估运营成本(元)', -- 假设每平米每天5元
    -- 利润指标
    COALESCE(SUM(cm.space_amount), 0) - 
    ROUND((COALESCE(e.reserve_area, 0) * 
           TIMESTAMPDIFF(DAY, e.arrangement_start_time, e.dismantling_end_time) * 5), 2)
    AS '预估毛利润(元)',
    -- 坪效分析
    ROUND(
        CASE WHEN e.reserve_area > 0 
        THEN COALESCE(SUM(cm.space_amount), 0) / e.reserve_area 
        ELSE 0 END, 2
    ) AS '单位面积收入(元/㎡)',
    -- 客户价值
    COUNT(DISTINCT be.id) AS '参展商数量',
    ROUND(
        CASE WHEN COUNT(DISTINCT be.id) > 0 
        THEN COALESCE(SUM(cm.space_amount), 0) / COUNT(DISTINCT be.id) 
        ELSE 0 END, 2
    ) AS '单展商贡献(元/家)'
FROM book_vb_exhibition e
LEFT JOIN book_vb_customer_main c ON e.customer_id = c.id AND c.is_enabled = 1
LEFT JOIN book_vb_contract_manage cm ON e.id = cm.exhibition_id 
    AND cm.contract_status = 'APPROVED'
    AND cm.is_enabled = 1
LEFT JOIN builder_bd_exhibitor be ON e.id = be.exhibition_manage_id 
    AND be.is_enabled = 1
WHERE e.is_deleted = 0 AND e.is_enabled = 1
    AND e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY e.id, e.exhibition_name, e.exhibition_no, e.exhibition_type, 
         e.exhibition_main_type, e.exhibition_nature, c.customer_cn_name,
         e.reserve_area, e.arrangement_start_time, e.dismantling_end_time
ORDER BY COALESCE(SUM(cm.space_amount), 0) DESC;


-- 展会运营效率
-- 指标：报馆、审核、搭建、证件等流程效率
-- 价值：评估展会运营效率，优化业务流程
SELECT 
    e.id AS '展会ID',
    e.exhibition_name AS '展会名称',
    e.exhibition_start_time AS '开展时间',
    -- 报馆效率
    COUNT(DISTINCT rs.id) AS '报馆申请总数',
    COUNT(DISTINCT CASE WHEN rs.report_space_status = 'APPROVED' THEN rs.id END) AS '报馆通过数',
    ROUND(
        COUNT(DISTINCT CASE WHEN rs.report_space_status = 'APPROVED' THEN rs.id END) * 100.0 /
        NULLIF(COUNT(DISTINCT rs.id), 0), 2
    ) AS '报馆通过率(%)',
    -- 图纸审核效率
    ROUND(AVG(
        CASE WHEN rda.audit_time IS NOT NULL AND rda.report_time IS NOT NULL
        THEN TIMESTAMPDIFF(HOUR, rda.report_time, rda.audit_time)
        ELSE NULL END
    ), 1) AS '图纸平均审核时长(小时)',
    -- 搭建验收效率
    COUNT(DISTINCT bar.id) AS '展台验收总数',
    COUNT(DISTINCT CASE WHEN bar.acceptance_status = 'PASS' THEN bar.id END) AS '验收通过数',
    ROUND(
        COUNT(DISTINCT CASE WHEN bar.acceptance_status = 'PASS' THEN bar.id END) * 100.0 /
        NULLIF(COUNT(DISTINCT bar.id), 0), 2
    ) AS '验收通过率(%)',
    -- 证件办理效率
    COUNT(DISTINCT cu.id) AS '制证申请总数',
    COUNT(DISTINCT CASE WHEN cu.certificate_status IN ('PRINT', 'DRAW', 'END') THEN cu.id END) AS '已制证数量',
    ROUND(
        COUNT(DISTINCT CASE WHEN cu.certificate_status IN ('PRINT', 'DRAW', 'END') THEN cu.id END) * 100.0 /
        NULLIF(COUNT(DISTINCT cu.id), 0), 2
    ) AS '制证完成率(%)',
    -- 维修响应
    COUNT(DISTINCT brr.id) AS '维修报修总数',
    COUNT(DISTINCT CASE WHEN brr.repair_status = 'COMPLETE' THEN brr.id END) AS '已修复数量',
    ROUND(AVG(
        CASE WHEN brr.report_time IS NOT NULL AND brr.update_time IS NOT NULL
        THEN TIMESTAMPDIFF(HOUR, brr.report_time, brr.update_time)
        ELSE NULL END
    ), 1) AS '平均修复时长(小时)'
FROM book_vb_exhibition e
LEFT JOIN builder_bd_report_space rs ON e.id = rs.exhibition_id AND rs.is_enabled = 1
LEFT JOIN hc_hc_report_drawing_category_audit rda ON rs.id = rda.report_space_id 
    AND rda.is_enabled = 1
LEFT JOIN hc_hc_booth_acceptance_record bar ON e.id = bar.exhibition_manage_id 
    AND bar.is_enabled = 1
LEFT JOIN cert_cert_certificate_user cu ON e.id = cu.exhibition_id 
    AND cu.is_enabled = 1
LEFT JOIN hc_hc_builder_repair_report brr ON e.id = brr.exhibition_manage_id 
    AND brr.is_enabled = 1
WHERE e.is_deleted = 0 AND e.is_enabled = 1
    AND e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY e.id, e.exhibition_name, e.exhibition_start_time
ORDER BY e.exhibition_start_time DESC;


-- 展会增长与趋势分析
-- 指标：展会数量、规模、收入增长趋势
-- 价值：分析展会发展趋势，制定市场策略
SELECT 
    -- 时间维度
    YEAR(e.exhibition_start_time) AS '年份',
    QUARTER(e.exhibition_start_time) AS '季度',
    -- 展会数量趋势
    COUNT(DISTINCT e.id) AS '展会数量',
    COUNT(DISTINCT CASE WHEN e.exhibition_type = 'EXHIBITION' THEN e.id END) AS '展会类型数量',
    COUNT(DISTINCT CASE WHEN e.exhibition_type = 'MEETING' THEN e.id END) AS '会议类型数量',
    COUNT(DISTINCT CASE WHEN e.exhibition_type = 'FORUM' THEN e.id END) AS '论坛类型数量',
    -- 规模趋势
    ROUND(AVG(e.reserve_area), 2) AS '平均预定面积(㎡)',
    ROUND(AVG(e.expect_viewer), 0) AS '平均预期人数',
    ROUND(AVG(TIMESTAMPDIFF(DAY, e.exhibition_start_time, e.exhibition_end_time)), 1) AS '平均展期(天)',
    -- 收入趋势
    COUNT(DISTINCT cm.id) AS '签约合同数',
    ROUND(SUM(COALESCE(cm.contract_amount, 0)), 2) AS '合同总金额',
    ROUND(AVG(COALESCE(cm.contract_amount, 0)), 2) AS '平均合同金额',
    ROUND(SUM(COALESCE(cm.space_amount, 0)), 2) AS '场地费收入',
    -- 客户增长
    COUNT(DISTINCT c.id) AS '客户数量',
    COUNT(DISTINCT CASE WHEN c.customer_level = '高' THEN c.id END) AS '高价值客户数',
    -- 同比增长率
    LAG(COUNT(DISTINCT e.id)) OVER (ORDER BY YEAR(e.exhibition_start_time), QUARTER(e.exhibition_start_time)) AS '上期展会数',
    ROUND(
        (COUNT(DISTINCT e.id) - LAG(COUNT(DISTINCT e.id)) OVER (ORDER BY YEAR(e.exhibition_start_time), QUARTER(e.exhibition_start_time))) * 100.0 /
        NULLIF(LAG(COUNT(DISTINCT e.id)) OVER (ORDER BY YEAR(e.exhibition_start_time), QUARTER(e.exhibition_start_time)), 0), 2
    ) AS '展会数量增长率(%)',
    -- 展商增长
    COUNT(DISTINCT be.id) AS '展商总数',
    ROUND(AVG(COUNT(DISTINCT be.id)) OVER (PARTITION BY YEAR(e.exhibition_start_time)), 0) AS '年平均展商数'
FROM book_vb_exhibition e
LEFT JOIN book_vb_contract_manage cm ON e.id = cm.exhibition_id 
    AND cm.contract_status = 'APPROVED'
    AND cm.is_enabled = 1
LEFT JOIN book_vb_customer_main c ON e.customer_id = c.id AND c.is_enabled = 1
LEFT JOIN builder_bd_exhibitor be ON e.id = be.exhibition_manage_id 
    AND be.is_enabled = 1
WHERE e.is_deleted = 0 AND e.is_enabled = 1
    AND e.exhibition_start_time >= DATE_SUB(CURDATE(), INTERVAL 24 MONTH)
GROUP BY YEAR(e.exhibition_start_time), QUARTER(e.exhibition_start_time)
HAVING COUNT(DISTINCT e.id) > 0
ORDER BY YEAR(e.exhibition_start_time) DESC, QUARTER(e.exhibition_start_time) DESC;



-- 展会全生命周期监控
-- 指标：展会各阶段进度、完成度、时间节点
-- 价值：实时监控展会进度，确保按时完成
SELECT 
    e.exhibition_name AS '展会名称',
    e.exhibition_start_time AS '开展时间',
    CASE e.state
        WHEN 'STATE_ONE' THEN '无状态'
        WHEN 'STATE_TWO' THEN '销售商机'
        WHEN 'STATE_THREE' THEN '报价确认'
        WHEN 'STATE_FOUR' THEN '活动确认'
        WHEN 'STATE_FIVE' THEN '创立合同'
        WHEN 'STATE_SIX' THEN '合同付款'
        WHEN 'STATE_SEVEN' THEN '活动结算'
        WHEN 'STATE_EIGHT' THEN '活动结束'
        WHEN 'STATE_NINE' THEN '其他状态'
        ELSE e.state  -- 如果出现未知状态，显示原值
    END AS '当前状态',
    -- 时间进度
    DATEDIFF(CURDATE(), e.create_time) AS '已进行天数',
    DATEDIFF(e.exhibition_start_time, CURDATE()) AS '距开展剩余天数',
    -- 各阶段完成情况
    CASE WHEN e.is_enabled = 1 THEN '已确认' ELSE '未确认' END AS '确认状态',
    CASE WHEN cm.id IS NOT NULL THEN '有合同' ELSE '无合同' END AS '合同状态',
    CASE WHEN COUNT(DISTINCT es.space_id) > 0 THEN '已预定' ELSE '未预定' END AS '场地预定状态',
    CASE WHEN COUNT(DISTINCT rs.id) > 0 THEN '已报馆' ELSE '未报馆' END AS '报馆状态',
    CASE WHEN COUNT(DISTINCT cu.id) > 0 THEN '已制证' ELSE '未制证' END AS '制证状态',
    -- 关键节点完成度
    ROUND(
        (CASE WHEN e.is_enabled = 1 THEN 1 ELSE 0 END +
         CASE WHEN cm.id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN COUNT(DISTINCT es.space_id) > 0 THEN 1 ELSE 0 END +
         CASE WHEN COUNT(DISTINCT rs.id) > 0 THEN 1 ELSE 0 END +
         CASE WHEN COUNT(DISTINCT cu.id) > 0 THEN 1 ELSE 0 END) * 100.0 / 5, 2
    ) AS '整体完成度(%)',
    -- 关键时间节点
    e.create_time AS '创建时间',
    MAX(cm.signing_time) AS '合同签署时间',
    MIN(es.create_time) AS '场地预定时间',
    MAX(rs.create_time) AS '首次报馆时间',
    MAX(cu.create_time) AS '首批制证时间',
    -- 预警提醒
    CASE 
        WHEN DATEDIFF(e.exhibition_start_time, CURDATE()) BETWEEN 0 AND 7 THEN '紧急：一周内开展'
        WHEN DATEDIFF(e.exhibition_start_time, CURDATE()) BETWEEN 8 AND 30 THEN '警告：一月内开展'
        WHEN DATEDIFF(e.exhibition_start_time, CURDATE()) > 30 THEN '正常'
        WHEN DATEDIFF(e.exhibition_start_time, CURDATE()) < 0 THEN '已结束'
    END AS '时间预警',
    -- 待办事项 (修正字符串拼接方式)
    CONCAT(
        CASE WHEN cm.id IS NULL THEN '需签订合同;' ELSE '' END,
        CASE WHEN COUNT(DISTINCT es.space_id) = 0 THEN '需预定场地;' ELSE '' END,
        CASE WHEN COUNT(DISTINCT rs.id) = 0 AND DATEDIFF(e.exhibition_start_time, CURDATE()) < 60 THEN '需报馆;' ELSE '' END,
        CASE WHEN COUNT(DISTINCT cu.id) = 0 AND DATEDIFF(e.exhibition_start_time, CURDATE()) < 30 THEN '需制证;' ELSE '' END
    ) AS '待办事项'
FROM book_vb_exhibition e
LEFT JOIN book_vb_contract_manage cm ON e.id = cm.exhibition_id 
    AND cm.is_enabled = 1
LEFT JOIN book_vb_exhibition_space es ON e.id = es.exhibition_id 
    AND es.is_enabled = 1
LEFT JOIN builder_bd_report_space rs ON e.id = rs.exhibition_id 
    AND rs.is_enabled = 1
LEFT JOIN cert_cert_certificate_user cu ON e.id = cu.exhibition_id 
    AND cu.is_enabled = 1
WHERE e.is_deleted = 0
    AND e.exhibition_start_time >= CURDATE()  -- 仅显示未开展的展会
GROUP BY e.id, e.exhibition_name, e.exhibition_start_time, e.state,
         e.create_time, e.is_enabled, cm.id
ORDER BY e.exhibition_start_time ASC;