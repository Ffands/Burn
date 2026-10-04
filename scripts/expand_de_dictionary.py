import gzip

de_map = {}

# Read current dict_de_ru
with gzip.open('app/src/main/assets/dict_de_ru.tsv.gz', 'rt', encoding='utf-8') as f:
    for line in f:
        p = line.strip().split('\t')
        if len(p) == 2: de_map[p[0]] = p[1]

# German numbers
nums_de = [
    ("null", "ноль"), ("eins", "один"), ("zwei", "два"), ("drei", "три"), ("vier", "четыре"),
    ("fünf", "пять"), ("sechs", "шесть"), ("sieben", "семь"), ("acht", "восемь"), ("neun", "девять"),
    ("zehn", "десять"), ("elf", "одиннадцать"), ("zwölf", "двенадцать"), ("dreizehn", "тринадцать"),
    ("vierzehn", "четырнадцать"), ("fünfzehn", "пятнадцать"), ("sechzehn", "шестнадцать"),
    ("siebzehn", "семнадцать"), ("achtzehn", "восемнадцать"), ("neunzehn", "девятнадцать"),
    ("zwanzig", "двадцать"), ("dreißig", "тридцать"), ("vierzig", "сорок"), ("fünfzig", "пятьдесят"),
    ("sechzig", "шестьдесят"), ("siebzig", "семьдесят"), ("achtzig", "восемьдесят"), ("neunzig", "девяносто"),
    ("hundert", "сто"), ("tausend", "тысяча"), ("million", "миллион")
]
for k, v in nums_de:
    de_map[k] = v

# Calendar
cal_de = [
    ("montag", "понедельник"), ("dienstag", "вторник"), ("mittwoch", "среда"),
    ("donnerstag", "четверг"), ("freitag", "пятница"), ("samstag", "суббота"), ("sonntag", "воскресенье"),
    ("januar", "январь"), ("februar", "февраль"), ("märz", "март"), ("april", "апрель"),
    ("mai", "май"), ("juni", "июнь"), ("juli", "июль"), ("august", "август"),
    ("september", "сентябрь"), ("oktober", "октябрь"), ("november", "ноябрь"), ("dezember", "декабрь")
]
for k, v in cal_de:
    de_map[k] = v

# German vocabulary expansion
vocab_de = [
    ("zeit", "время"), ("jahr", "год"), ("jahre", "годы / лет"), ("mensch", "человек"),
    ("menschen", "люди"), ("tag", "день"), ("tage", "дни"), ("mann", "мужчина"),
    ("männer", "мужчины"), ("frau", "женщина"), ("frauen", "женщины"), ("kind", "ребенок"),
    ("kinder", "дети"), ("leben", "жизнь"), ("welt", "мир"), ("hand", "рука"),
    ("hände", "руки"), ("auge", "глаз"), ("augen", "глаза"), ("stadt", "город"),
    ("städte", "города"), ("land", "страна / земля"), ("länder", "страны"),
    ("haus", "дом"), ("häuser", "дома"), ("freund", "друг"), ("freunde", "друзья"),
    ("arbeit", "работа"), ("arbeiten", "работать"), ("schule", "школа"), ("schulen", "школы"),
    ("frage", "вопрос"), ("fragen", "вопросы"), ("antwort", "ответ"), ("antworten", "ответы"),
    ("buch", "книга"), ("bücher", "книги"), ("wort", "слово"), ("wörter", "слова"),
    ("geld", "деньги"), ("monat", "месяц"), ("monate", "месяцы"), ("woche", "неделя"),
    ("wochen", "недели"), ("stunde", "час"), ("stunden", "часы"), ("minute", "минута"),
    ("minuten", "минуты"), ("sekunde", "секунда"), ("sekunden", "секунды"),
    ("abend", "вечер"), ("morgen", "утро"), ("nacht", "ночь"),
    ("name", "имя"), ("wasser", "вода"), ("feuer", "огонь"), ("erde", "земля"),
    ("luft", "воздух"), ("auto", "автомобиль"), ("zug", "поезд"), ("flugzeug", "самолет"),
    ("straße", "улица"), ("platz", "площадь / место"), ("zimmer", "комната"),
    ("tür", "дверь"), ("fenster", "окно"), ("tisch", "стол"), ("stuhl", "стул"),
    ("telefon", "телефон"), ("bild", "картина / фото"), ("musik", "музыка"),
    ("film", "фильм"), ("spiel", "игра"), ("spiele", "игры"), ("spieler", "игрок"),
    ("essen", "еда / кушать"), ("trinken", "пить"), ("kaffee", "кофе"), ("tee", "чай"),
    ("brot", "хлеб"), ("fleisch", "мясо"), ("fisch", "рыба"), ("apfel", "яблоко"),
    ("bier", "пиво"), ("wein", "вино"), ("milch", "молоко"), ("zucker", "сахар"),
    ("salz", "соль"), ("restaurant", "ресторан"), ("hotel", "отель"), ("bahnhof", "вокзал"),
    ("flughafen", "аэропорт"), ("arzt", "врач"), ("ärztin", "женщина-врач"),
    ("krankenhaus", "больница"), ("apotheke", "аптека"), ("polizei", "полиция"),
    ("post", "почта"), ("bank", "банк"), ("geschäft", "магазин"), ("markt", "рынок"),
    ("preis", "цена"), ("rechnung", "счет"), ("karte", "карта"), ("brief", "письмо"),
    ("nachricht", "сообщение"), ("problem", "проблема"), ("hilfe", "помощь"),
    ("anfang", "начало"), ("ende", "конец"), ("ziel", "цель"), ("erfolg", "успех"),
    ("glück", "счастье / удача"), ("liebe", "любовь"), ("hoffnung", "надежда"),
    ("angst", "страх"), ("ruhe", "покой"), ("frieden", "мир"), ("krieg", "война"),
    ("sonne", "солнце"), ("mond", "луна"), ("stern", "звезда"), ("sterne", "звезды"),
    ("himmel", "небо"), ("regen", "дождь"), ("schnee", "снег"), ("wind", "ветер"),
    ("wetter", "погода"), ("sommer", "лето"), ("winter", "зима"), ("frühling", "весна"),
    ("herbst", "осень"), ("baum", "дерево"), ("bäume", "деревья"), ("blume", "цветок"),
    ("wald", "лес"), ("berg", "гора"), ("berge", "горы"), ("meer", "море"),
    ("fluss", "река"), ("see", "озеро"), ("insel", "остров"), ("strand", "пляж"),
    ("tier", "животное"), ("tiere", "животные"), ("hund", "собака"), ("katze", "кошка"),
    ("vogel", "птица"), ("pferd", "лошадь"), ("kuh", "корова"),
    ("kleidung", "одежда"), ("hemd", "рубашка"), ("hose", "брюки"), ("schuh", "туфля"),
    ("schuhe", "обувь"), ("jacke", "куртка"), ("mantel", "пальто"), ("mütze", "шапка"),
    ("körper", "тело"), ("kopf", "голова"), ("haar", "волосы"), ("haare", "волосы"),
    ("ohr", "ухо"), ("ohren", "уши"), ("nase", "нос"), ("mund", "рот"),
    ("zahn", "зуб"), ("zähne", "зубы"), ("hals", "шея / горло"), ("arm", "рука"),
    ("arme", "руки"), ("bein", "нога"), ("beine", "ноги"), ("fuß", "нога / ступня"),
    ("füße", "стопы"), ("herz", "сердце"), ("bauch", "живот"), ("rücken", "спина")
]

for k, v in vocab_de:
    de_map[k] = v

with gzip.open('app/src/main/assets/dict_de_ru.tsv.gz', 'wt', encoding='utf-8') as f:
    for k, v in sorted(de_map.items()):
        f.write(f"{k}\t{v}\n")

print("Generated expanded dict_de_ru.tsv.gz with", len(de_map), "terms")
