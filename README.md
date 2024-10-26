# 竞争性关键字推荐算法与系统设计
## 数据说明：
实验数据采用老师提供的搜狗比赛数据集，实验过程包含以下文件：
1. 原始数据：raw文件夹下的数据文件
2. 转化数据：
   1. query_words.train：原始数据初步提取的query词
   2. cleaned.train：在query_words.train基础上去除URL等无用数据
   3. seg_list.train：在cleaned.train基础上分词
   4. filter_list.train：在seg_list.train基础上去除停用词
3. 分词比较数据：使用icwb2-data中的数据作为测试集进行分词比较