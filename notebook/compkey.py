import jieba

# 关键词列表
keywords = [
    "图片::", "手机::", "小说::", "视频::", "下载::", "大全::", "qq::", "电影::", "中国::", "世界::",
    "重生::", "百度::", "官网::", "txt::", "英语::", "电视剧::", "游戏::", "查询::", "做法::", "倾城::"
]
# 加载自定义词典
jieba.load_userdict('../data/dictionary')
# 种子关键词列表
keywords_clean = [keyword[:-2] for keyword in keywords]
# 创建一个字典，用于存储包含种子关键词的搜索记录数
seed_dist = {keyword: 0 for keyword in keywords_clean}
# 创建一个字典，用于存储同时包含种子关键词和中介关键词的搜索记录数
mid_dist = {keyword: {} for keyword in keywords_clean}
# 当前处理的关键词
current_keyword = None

# 种子关键词搜索条目输入文件路径
seed_words_query_file_path = '../data/processed/seed_words_query.train'
# 输出文件路径
output_file_path = '../data/seg_mid.train'

# 1. 加载停用词
stopwords_file = '../data/stop_words/merge_stopwords.txt'  # 停用词文件路径
stopwords = set()
with open(stopwords_file, 'r', encoding='utf-8') as file:
    for line in file:
        stopwords.add(line.strip())

# 统计种子关键词对应的搜索条目的数量
# 计算a的具体时间
with open(seed_words_query_file_path, 'r', encoding='utf-8') as input_data:
    current_seed_keyword = None
    for line in input_data:
        line = line.strip()
        # 读取到新的关键词
        if any(keyword == line for keyword in keywords):
            # 重新赋值
            current_seed_keyword = line[:-2]
        else:
            # 统计包含种子关键词的搜索条目的数量
            seed_dist[current_seed_keyword] += 1

# 读取种子关键词搜索条目文件，将搜索条目分词
with open(seed_words_query_file_path, 'r', encoding='utf-8') as train_data:
    # 逐行处理
    for line in train_data:
        line = line.strip()
        # 判断是否为新关键词行
        if any(keyword == line for keyword in keywords):
            current_keyword = line[:-2]  # 去掉冒号
        else:
            # 使用lcut分词，返回结果为列表形式
            seg_list = [word for word in jieba.lcut(line) if word != '']
            # 去掉与当前关键词相同的部分，同时删除出现在停用词列表中的词
            filtered_segs = [seg for seg in seg_list if seg != current_keyword and seg not in stopwords]
            # 根据分词的结果，统计包含种子关键词和中介关键词的搜索记录数目，包在{}，统计sa
            for seg in filtered_segs:
                if seg not in mid_dist[current_keyword]:
                    mid_dist[current_keyword][seg] = {'freq': 0}
                else:
                    mid_dist[current_keyword][seg]['freq'] = mid_dist[current_keyword][seg]['freq'] + 1

# 每个种子关键词下只选取数目最多的前20个作为中介关键词
for keyword in mid_dist:
    sorted_mid_words = sorted(mid_dist[keyword].items(), key=lambda x: x[1]['freq'], reverse=True)
    mid_dist[keyword] = {mid_word: info for mid_word, info in sorted_mid_words[:20]}

# 计算权重
for keyword in mid_dist:
    for mid_word in mid_dist[keyword]:
        # 计算权重
        mid_dist[keyword][mid_word]['weight'] = mid_dist[keyword][mid_word]['freq'] / seed_dist[keyword]

# # 将结果写入文件
# with open(output_file_path, 'w', encoding='utf-8') as output_file:
#     for keyword in mid_dist:
#         for mid_word in mid_dist[keyword]:
#             # 获取权重
#             weight = mid_dist[keyword][mid_word]['weight']
#             # 将结果写入文件
#             output_file.write(f"{keyword}::{mid_word}::{weight}\n")
#
print(mid_dist)

