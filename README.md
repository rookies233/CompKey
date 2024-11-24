# FishFind 竞争性关键字推荐系统

## 核心功能

- 关键字分析与排序：利用CompKey算法，分析各关键字的竞争性，计算其综合价值并进行排序；
- 关键字推荐：根据用户输入的初始关键字，推荐出相关的、具有较高竞争力的关键字；
- 用户自定义筛选：提供过滤和筛选选项，如指定搜索量范围、竞争度等，以满足用户个性化需求；
- 用户管理：支持用户账户管理；
- 历史关键字管理：记录用户历史搜索和推荐结果，便于日后参考；
- 引入AI关键字分析：利用大语言模型进行关键字的深入分析。

## 数据说明：
实验数据采用老师提供的搜狗比赛数据集，实验过程包含以下文件：
1. 原始数据：raw文件夹下的数据文件
2. 转化数据：
   1. query_words.train：原始数据初步提取的query词
   2. cleaned.train：在query_words.train基础上去除URL等无用数据
   3. seg_list.train：在cleaned.train基础上分词
   4. filter_list.train：在seg_list.train基础上去除停用词
3. 分词比较数据：使用icwb2-data中的数据作为测试集进行分词比较
