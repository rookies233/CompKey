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
# 定义一个字典，用于存储每个词及其对应的分词结果
word_dict = {keyword: [] for keyword in keywords_clean}
# 创建一个字典，用于存储包含种子关键词的搜索记录数
seed_dist = {keyword: 0 for keyword in keywords_clean}
# 当前处理的关键词
current_keyword = None

# 种子关键词搜索条目输入文件路径
seed_words_query_file_path = '../data/seed_words_query.train'
# 输出文件路径
output_file_path = '../data/seg_mid.train'

# 统计种子关键词对应的搜索条目的数量
# ToDo:计算a的具体时间
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

            # 去掉与当前关键词相同的部分
            filtered_segs = [seg for seg in seg_list if seg != current_keyword]
            if current_keyword and filtered_segs:
                word_dict[current_keyword].extend(filtered_segs)

# 打开输出文件
with open(output_file_path, 'w', encoding='utf-8') as output_data:
    # 按词打印分词结果
    for keyword, segs in word_dict.items():
        output_data.write(f"{keyword}:\n")
        for seg in segs:
            output_data.write(f" {seg}\n")

# 1. 加载停用词
stopwords_file = '../data/stop_words/merge_stopwords.txt'  # 停用词文件路径
stopwords = set()

with open(stopwords_file, 'r', encoding='utf-8') as file:
    for line in file:
        stopwords.add(line.strip())

# 2. 读取已分词的训练数据并过滤停用词
train_file = '../data/seg_mid.train'  # 已分词的训练数据路径
output_file = '../data/filter_list_mid.train'  # 过滤后的训练数据路径

with open(train_file, 'r', encoding='utf-8') as train_data, \
        open(output_file, 'w', encoding='utf-8') as output_data:
    for line in train_data:
        line = line.strip()  # 去除行首尾空白
        words = line.split()  # 将分词结果按空格拆分
        # 过滤停用词
        filtered_words = [word for word in words if word not in stopwords]
        if filtered_words:  # 确保不写入空行
            output_data.write(' '.join(filtered_words) + '\n')  # 以空格连接过滤后的词
