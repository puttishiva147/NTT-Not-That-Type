package com.example.language

data class WordData(
    val script: String,
    val phonetic: String
)

object LanguageDictionary {

    // English word definitions 0-100
    private val englishWords = mapOf(
        0 to WordData("Zero", "Zero"),
        1 to WordData("One", "One"),
        2 to WordData("Two", "Two"),
        3 to WordData("Three", "Three"),
        4 to WordData("Four", "Four"),
        5 to WordData("Five", "Five"),
        6 to WordData("Six", "Six"),
        7 to WordData("Seven", "Seven"),
        8 to WordData("Eight", "Eight"),
        9 to WordData("Nine", "Nine"),
        10 to WordData("Ten", "Ten"),
        11 to WordData("Eleven", "Eleven"),
        12 to WordData("Twelve", "Twelve"),
        13 to WordData("Thirteen", "Thirteen"),
        14 to WordData("Fourteen", "Fourteen"),
        15 to WordData("Fifteen", "Fifteen"),
        16 to WordData("Sixteen", "Sixteen"),
        17 to WordData("Seventeen", "Seventeen"),
        18 to WordData("Eighteen", "Eighteen"),
        19 to WordData("Nineteen", "Nineteen"),
        20 to WordData("Twenty", "Twenty"),
        21 to WordData("Twenty-one", "Twenty-one"),
        22 to WordData("Twenty-two", "Twenty-two"),
        23 to WordData("Twenty-three", "Twenty-three"),
        24 to WordData("Twenty-four", "Twenty-four"),
        25 to WordData("Twenty-five", "Twenty-five"),
        26 to WordData("Twenty-six", "Twenty-six"),
        27 to WordData("Twenty-seven", "Twenty-seven"),
        28 to WordData("Twenty-eight", "Twenty-eight"),
        29 to WordData("Twenty-nine", "Twenty-nine"),
        30 to WordData("Thirty", "Thirty"),
        31 to WordData("Thirty-one", "Thirty-one"),
        32 to WordData("Thirty-two", "Thirty-two"),
        33 to WordData("Thirty-three", "Thirty-three"),
        34 to WordData("Thirty-four", "Thirty-four"),
        35 to WordData("Thirty-five", "Thirty-five"),
        36 to WordData("Thirty-six", "Thirty-six"),
        37 to WordData("Thirty-seven", "Thirty-seven"),
        38 to WordData("Thirty-eight", "Thirty-eight"),
        39 to WordData("Thirty-nine", "Thirty-nine"),
        40 to WordData("Forty", "Forty"),
        41 to WordData("Forty-one", "Forty-one"),
        42 to WordData("Forty-two", "Forty-two"),
        43 to WordData("Forty-three", "Forty-three"),
        44 to WordData("Forty-four", "Forty-four"),
        45 to WordData("Forty-five", "Forty-five"),
        46 to WordData("Forty-six", "Forty-six"),
        47 to WordData("Forty-seven", "Forty-seven"),
        48 to WordData("Forty-eight", "Forty-eight"),
        49 to WordData("Forty-nine", "Forty-nine"),
        50 to WordData("Fifty", "Fifty"),
        51 to WordData("Fifty-one", "Fifty-one"),
        52 to WordData("Fifty-two", "Fifty-two"),
        53 to WordData("Fifty-three", "Fifty-three"),
        54 to WordData("Fifty-four", "Fifty-four"),
        55 to WordData("Fifty-five", "Fifty-five"),
        56 to WordData("Fifty-six", "Fifty-six"),
        57 to WordData("Fifty-seven", "Fifty-seven"),
        58 to WordData("Fifty-eight", "Fifty-eight"),
        59 to WordData("Fifty-nine", "Fifty-nine"),
        60 to WordData("Sixty", "Sixty"),
        61 to WordData("Sixty-one", "Sixty-one"),
        62 to WordData("Sixty-two", "Sixty-two"),
        63 to WordData("Sixty-three", "Sixty-three"),
        64 to WordData("Sixty-four", "Sixty-four"),
        65 to WordData("Sixty-five", "Sixty-five"),
        66 to WordData("Sixty-six", "Sixty-six"),
        67 to WordData("Sixty-seven", "Sixty-seven"),
        68 to WordData("Sixty-eight", "Sixty-eight"),
        69 to WordData("Sixty-nine", "Sixty-nine"),
        70 to WordData("Seventy", "Seventy"),
        71 to WordData("Seventy-one", "Seventy-one"),
        72 to WordData("Seventy-two", "Seventy-two"),
        73 to WordData("Seventy-three", "Seventy-three"),
        74 to WordData("Seventy-four", "Seventy-four"),
        75 to WordData("Seventy-five", "Seventy-five"),
        76 to WordData("Seventy-six", "Seventy-six"),
        77 to WordData("Seventy-seven", "Seventy-seven"),
        78 to WordData("Seventy-eight", "Seventy-eight"),
        79 to WordData("Seventy-nine", "Seventy-nine"),
        80 to WordData("Eighty", "Eighty"),
        81 to WordData("Eighty-one", "Eighty-one"),
        82 to WordData("Eighty-two", "Eighty-two"),
        83 to WordData("Eighty-three", "Eighty-three"),
        84 to WordData("Eighty-four", "Eighty-four"),
        85 to WordData("Eighty-five", "Eighty-five"),
        86 to WordData("Eighty-six", "Eighty-six"),
        87 to WordData("Eighty-seven", "Eighty-seven"),
        88 to WordData("Eighty-eight", "Eighty-eight"),
        89 to WordData("Eighty-nine", "Eighty-nine"),
        90 to WordData("Ninety", "Ninety"),
        91 to WordData("Ninety-one", "Ninety-one"),
        92 to WordData("Ninety-two", "Ninety-two"),
        93 to WordData("Ninety-three", "Ninety-three"),
        94 to WordData("Ninety-four", "Ninety-four"),
        95 to WordData("Ninety-five", "Ninety-five"),
        96 to WordData("Ninety-six", "Ninety-six"),
        97 to WordData("Ninety-seven", "Ninety-seven"),
        98 to WordData("Ninety-eight", "Ninety-eight"),
        99 to WordData("Ninety-nine", "Ninety-nine"),
        100 to WordData("One hundred", "One hundred")
    )

    // Hindi 0-100 complete mapping
    val hindiWords = mapOf(
        0 to WordData("शून्य", "Shunya"),
        1 to WordData("एक", "Ek"),
        2 to WordData("दो", "Do"),
        3 to WordData("तीन", "Teen"),
        4 to WordData("चार", "Chaar"),
        5 to WordData("पाँच", "Paanch"),
        6 to WordData("छह", "Chhah"),
        7 to WordData("सात", "Saat"),
        8 to WordData("आठ", "Aath"),
        9 to WordData("नौ", "Nau"),
        10 to WordData("दस", "Das"),
        11 to WordData("ग्यारह", "Gyarah"),
        12 to WordData("बारह", "Barah"),
        13 to WordData("तेरह", "Terah"),
        14 to WordData("चौदह", "Chaudah"),
        15 to WordData("पंद्रह", "Pandrah"),
        16 to WordData("सोलह", "Solah"),
        17 to WordData("सत्रह", "Satrah"),
        18 to WordData("अठारह", "Atharah"),
        19 to WordData("उन्नीस", "Unnees"),
        20 to WordData("बीस", "Bees"),
        21 to WordData("इक्कीस", "Ikkees"),
        22 to WordData("बाईस", "Baees"),
        23 to WordData("तेईस", "Taees"),
        24 to WordData("चौबीस", "Chaubees"),
        25 to WordData("पच्चीस", "Pachchees"),
        26 to WordData("छब्बीस", "Chhabbees"),
        27 to WordData("सत्ताईस", "Sattaees"),
        28 to WordData("अट्ठाईस", "Atthaees"),
        29 to WordData("उनतीस", "Untees"),
        30 to WordData("तीस", "Tees"),
        31 to WordData("इकत्तीस", "Ikattees"),
        32 to WordData("बत्तीस", "Battees"),
        33 to WordData("तैंतीस", "Taintees"),
        34 to WordData("चौंतीस", "Chauntees"),
        35 to WordData("पैंतीस", "Paintees"),
        36 to WordData("छत्तीस", "Chhattees"),
        37 to WordData("सैंतीस", "Saintees"),
        38 to WordData("अड़तीस", "Artees"),
        39 to WordData("उनतालीस", "Untalees"),
        40 to WordData("चालीस", "Chalees"),
        41 to WordData("इकतालीस", "Iktalees"),
        42 to WordData("बयालीस", "Bayalees"),
        43 to WordData("तैंतालीस", "Taintalees"),
        44 to WordData("चवालीस", "Chawalees"),
        45 to WordData("पैंतालीस", "Paintalees"),
        46 to WordData("छियालीस", "Chhiyalees"),
        47 to WordData("सैंतालीस", "Saintalees"),
        48 to WordData("अड़तालीस", "Artalees"),
        49 to WordData("उनचास", "Unchaas"),
        50 to WordData("पचास", "Pachaas"),
        51 to WordData("इक्यावन", "Ikyaavan"),
        52 to WordData("बावन", "Baawan"),
        53 to WordData("तिरेपन", "Tirepan"),
        54 to WordData("चौवन", "Chauwan"),
        55 to WordData("पचपन", "Pachpan"),
        56 to WordData("छप्पन", "Chhappan"),
        57 to WordData("सत्तावन", "Sattawan"),
        58 to WordData("अट्ठावन", "Atthawan"),
        59 to WordData("उनसठ", "Unsath"),
        60 to WordData("साठ", "Saath"),
        61 to WordData("इकसठ", "Iksath"),
        62 to WordData("बासठ", "Baasath"),
        63 to WordData("तिरसठ", "Tirsath"),
        64 to WordData("चौंसठ", "Chaunsath"),
        65 to WordData("पैंसठ", "Painsath"),
        66 to WordData("छियासठ", "Chhiyaasath"),
        67 to WordData("सरसठ", "Sarsath"),
        68 to WordData("अड़सठ", "Arsath"),
        69 to WordData("उनहत्तर", "Unhattar"),
        70 to WordData("सत्तर", "Sattar"),
        71 to WordData("इकहत्तर", "Ikhattar"),
        72 to WordData("बहत्तर", "Bahattar"),
        73 to WordData("तिहत्तर", "Tihattar"),
        74 to WordData("चौहत्तर", "Chauhattar"),
        75 to WordData("पचहत्तर", "Pachhattar"),
        76 to WordData("छिहत्तर", "Chhihattar"),
        77 to WordData("सतहत्तर", "Sat-hattar"),
        78 to WordData("अठहत्तर", "Athhattar"),
        79 to WordData("उन्नासी", "Unnasi"),
        80 to WordData("अस्सी", "Assi"),
        81 to WordData("इक्यासी", "Ikyasi"),
        82 to WordData("बयासी", "Bayasi"),
        83 to WordData("तिरासी", "Tirasi"),
        84 to WordData("चौरासी", "Chaurasi"),
        85 to WordData("पचासी", "Pachasi"),
        86 to WordData("छियासी", "Chhiyasi"),
        87 to WordData("सतासी", "Satasi"),
        88 to WordData("अठासी", "Athasi"),
        89 to WordData("नवासी", "Navasi"),
        90 to WordData("नब्बे", "Nabbee"),
        91 to WordData("इक्यानवे", "Ikyanve"),
        92 to WordData("बानवे", "Baanve"),
        93 to WordData("तिरानवे", "Tiranve"),
        94 to WordData("चौरानवे", "Chauranve"),
        95 to WordData("पंचानवे", "Panchanve"),
        96 to WordData("छियानवे", "Chhiyanve"),
        97 to WordData("सत्तानवे", "Sattanve"),
        98 to WordData("अट्ठानवे", "Atthanve"),
        99 to WordData("निन्यानवे", "Ninyanve"),
        100 to WordData("सौ", "Sau")
    )

    // Marathi 0-100 mapping
    val marathiWords = mapOf(
        0 to WordData("शून्य", "Shunya"),
        1 to WordData("एक", "Ek"),
        2 to WordData("दोन", "Don"),
        3 to WordData("तीन", "Teen"),
        4 to WordData("चार", "Chaar"),
        5 to WordData("पाच", "Paach"),
        6 to WordData("सहा", "Saha"),
        7 to WordData("सात", "Saat"),
        8 to WordData("आठ", "Aath"),
        9 to WordData("नऊ", "Nau"),
        10 to WordData("दहा", "Daha"),
        11 to WordData("अकरा", "Akara"),
        12 to WordData("बारा", "Bara"),
        13 to WordData("तेरा", "Tera"),
        14 to WordData("चौदा", "Chauda"),
        15 to WordData("पंधरा", "Pandhra"),
        16 to WordData("सोळा", "Sola"),
        17 to WordData("सतरा", "Satra"),
        18 to WordData("अठरा", "Athara"),
        19 to WordData("एकोणीस", "Ekonis"),
        20 to WordData("वीस", "Vis"),
        21 to WordData("एकवीस", "Ekvis"),
        22 to WordData("बावीस", "Bavis"),
        23 to WordData("तेवीस", "Tevis"),
        24 to WordData("चोवीस", "Chovis"),
        25 to WordData("पंचवीस", "Panchvis"),
        26 to WordData("सव्वीस", "Savvis"),
        27 to WordData("सत्तावीस", "Sattavis"),
        28 to WordData("अठ्ठावीस", "Atthavis"),
        29 to WordData("एकोणतीस", "Ekontis"),
        30 to WordData("तीस", "Tis"),
        31 to WordData("एकतीस", "Ektis"),
        32 to WordData("बत्तीस", "Battis"),
        33 to WordData("तेहतीस", "Tehtis"),
        34 to WordData("चौतीस", "Chautis"),
        35 to WordData("पस्तीस", "Pastis"),
        36 to WordData("छत्तीस", "Chhattis"),
        37 to WordData("सदतीस", "Sadtis"),
        38 to WordData("अडतीस", "Adtis"),
        39 to WordData("एकेचाळीस", "Ekechalis"),
        40 to WordData("चाळीस", "Chalis"),
        41 to WordData("एक्केचाळीस", "Ekkechalis"),
        42 to WordData("बेचाळीस", "Bechalis"),
        43 to WordData("त्रेचाळीस", "Trechalis"),
        44 to WordData("चव्वेचाळीस", "Chavvechalis"),
        45 to WordData("पंचेचाळीस", "Panchechalis"),
        46 to WordData("शेचाळीस", "Shechalis"),
        47 to WordData("सत्तेचाळीस", "Sattechalis"),
        48 to WordData("अठ्ठेचाळीस", "Aththechalis"),
        49 to WordData("एकोणपन्नास", "Ekonpannas"),
        50 to WordData("पन्नास", "Pannas"),
        51 to WordData("एक्कावन्न", "Ekkavanna"),
        52 to WordData("बावन्न", "Bavanna"),
        53 to WordData("त्रेपन्न", "Trepanna"),
        54 to WordData("चोपन्न", "Chopanna"),
        55 to WordData("पंचावन्न", "Panchavanna"),
        56 to WordData("छप्पन्न", "Chhappanna"),
        57 to WordData("सत्तावन्न", "Sattavanna"),
        58 to WordData("अठ्ठावन्न", "Atthavanna"),
        59 to WordData("एकोणसाठ", "Ekonsath"),
        60 to WordData("साठ", "Saath"),
        61 to WordData("एकसष्ठ", "Ekasastha"),
        62 to WordData("पासष्ठ", "Baasastha"),
        63 to WordData("त्रेसष्ठ", "Tresastha"),
        64 to WordData("चौसष्ठ", "Chausastha"),
        65 to WordData("पासष्ठ", "Pasastha"),
        66 to WordData("सहासष्ठ", "Sahasastha"),
        67 to WordData("सदुसष्ठ", "Sadusastha"),
        68 to WordData("अडुसष्ठ", "Adusastha"),
        69 to WordData("एकोणसत्तर", "Ekonsattar"),
        70 to WordData("सत्तर", "Sattar"),
        71 to WordData("एकाहत्तर", "Ekahattar"),
        72 to WordData("बाहत्तर", "Bahattar"),
        73 to WordData("त्र्याहत्तर", "Tryahattar"),
        74 to WordData("चौर्‍याहत्तर", "Chauryahattar"),
        75 to WordData("पंच्याहत्तर", "Panchyahattar"),
        76 to WordData("शहात्तर", "Shahattar"),
        77 to WordData("सत्त्याहत्तर", "Sattyahattar"),
        78 to WordData("अठ्ठ्याहत्तर", "Aththyahattar"),
        79 to WordData("एकोणऐंशी", "Ekon-aishi"),
        80 to WordData("ऐंशी", "Ainshi"),
        81 to WordData("एक्याऐंशी", "Ekya-aishi"),
        82 to WordData("ब्याऐंशी", "Bya-aishi"),
        83 to WordData("त्र्याऐंशी", "Trya-aishi"),
        84 to WordData("चौऱ्याऐंशी", "Chourya-aishi"),
        85 to WordData("पंच्याऐंशी", "Panchya-aishi"),
        86 to WordData("शहाऐंशी", "Shaha-aishi"),
        87 to WordData("सत्त्याऐंशी", "Sattya-aishi"),
        88 to WordData("अठ्ठ्याऐंशी", "Aththya-aishi"),
        89 to WordData("एकोणनव्वद", "Ekonnavvad"),
        90 to WordData("नव्वद", "Navvad"),
        91 to WordData("एक्याण्णव", "Ekyannav"),
        92 to WordData("ब्याण्णव", "Byannav"),
        93 to WordData("त्र्याण्णव", "Tryannav"),
        94 to WordData("चौऱ्याण्णव", "Chouryannav"),
        95 to WordData("पंच्याण्णव", "Panchyannav"),
        96 to WordData("शहाण्णव", "Shahannav"),
        97 to WordData("सत्त्याण्णव", "Sattyannav"),
        98 to WordData("अठ्ठ्याण्णव", "Aththyannav"),
        99 to WordData("नव्व्याण्णव", "Navvyannav"),
        100 to WordData("शंभर", "Shambhar")
    )

    // Gujarati 0-100 mapping
    val gujaratiWords = mapOf(
        0 to WordData("શૂન્ય", "Shunya"),
        1 to WordData("એક", "Ek"),
        2 to WordData("બે", "Be"),
        3 to WordData("ત્રણ", "Tran"),
        4 to WordData("ચાર", "Chaar"),
        5 to WordData("પાંચ", "Paanch"),
        6 to WordData("છ", "Chha"),
        7 to WordData("સાત", "Saat"),
        8 to WordData("આઠ", "Aath"),
        9 to WordData("નવ", "Nav"),
        10 to WordData("દસ", "Das"),
        20 to WordData("વીસ", "Vis"),
        24 to WordData("ચોવીસ", "Chovis"),
        25 to WordData("પચીસ", "Pachis"),
        30 to WordData("ત્રીસ", "Tris"),
        40 to WordData("ચાલીસ", "Chalis"),
        50 to WordData("પચાસ", "Pachaas"),
        55 to WordData("પંચાવન", "Panchavan"),
        57 to WordData("સત્તાવન", "Sattavan"),
        60 to WordData("સાઠ", "Saath"),
        70 to WordData("સિત્તેર", "Sitter"),
        78 to WordData("ઇઠ્યોતેર", "Ithyoter"),
        80 to WordData("એંસી", "Ensi"),
        90 to WordData("નેવું", "Nevu"),
        99 to WordData("નવાણું", "Navanu"),
        100 to WordData("સો", "So")
    )

    // Bengali 0-100 mapping
    val bengaliWords = mapOf(
        0 to WordData("শূন্য", "Shunyo"),
        1 to WordData("এক", "Ek"),
        2 to WordData("দুই", "Dui"),
        3 to WordData("তিন", "Tin"),
        4 to WordData("চার", "Char"),
        5 to WordData("পাঁচ", "Panch"),
        6 to WordData("ছয়", "Chhoy"),
        7 to WordData("সাত", "Saat"),
        8 to WordData("আট", "Aat"),
        9 to WordData("নয়", "Noy"),
        10 to WordData("দশ", "Dosh"),
        20 to WordData("কুড়ি", "Kuri"),
        24 to WordData("চব্বিশ", "Chobbish"),
        25 to WordData("পঁচিশ", "Pnochish"),
        30 to WordData("ত্রিশ", "Trish"),
        40 to WordData("চল্লিশ", "Chollish"),
        50 to WordData("পঞ্চাশ", "Ponchash"),
        55 to WordData("পঞ্চান্ন", "Ponchanno"),
        57 to WordData("সাতান্ন", "Satanno"),
        60 to WordData("ষাট", "Shaat"),
        70 to WordData("সত্তর", "Shottor"),
        78 to WordData("আটাত্তর", "Atattor"),
        80 to WordData("আশি", "Ashi"),
        90 to WordData("নব্বই", "Nobboi"),
        99 to WordData("নিরানব্বই", "Niranobboi"),
        100 to WordData("এক শত", "Ek shoto")
    )

    // Tamil 0-100 mapping
    val tamilWords = mapOf(
        0 to WordData("பூஜ்ஜியம்", "Poojjiyam"),
        1 to WordData("ஒன்று", "Ondru"),
        2 to WordData("இரண்டு", "Irandu"),
        3 to WordData("மூன்று", "Moondru"),
        4 to WordData("நான்கு", "Naangu"),
        5 to WordData("ஐந்து", "Ainthu"),
        6 to WordData("ஆறு", "Aaru"),
        7 to WordData("ஏழு", "Ezhu"),
        8 to WordData("எட்டு", "Ettu"),
        9 to WordData("ஒன்பது", "Onpathu"),
        10 to WordData("பத்து", "Pathu"),
        20 to WordData("இருபது", "Irupathu"),
        24 to WordData("இருபத்து நான்கு", "Irupathu Naangu"),
        25 to WordData("இருபத்தைந்து", "Irupaththainthu"),
        30 to WordData("முப்பது", "Muppathu"),
        40 to WordData("நாற்பது", "Naarpathu"),
        50 to WordData("ஐம்பது", "Aimpathu"),
        55 to WordData("ஐம்பத்தைந்து", "Aimbaththeinthu"),
        57 to WordData("ஐம்பத்தேழு", "Aimpaththezhu"),
        60 to WordData("அறுபது", "Arupathu"),
        70 to WordData("எழுபது", "Ezhupathu"),
        78 to WordData("எழுபத்தெட்டு", "Ezhupaththettu"),
        80 to WordData("எண்பது", "Enbathu"),
        90 to WordData("தொண்ணூறு", "Thonnooru"),
        99 to WordData("தொண்ணூற்று ஒன்பது", "Thonnoottru Onpathu"),
        100 to WordData("நூறு", "Nooru")
    )

    // Telugu 0-100 mapping
    val teluguWords = mapOf(
        0 to WordData("సున్నా", "Sunna"),
        1 to WordData("ఒకటి", "Okati"),
        2 to WordData("రెండు", "Rendu"),
        3 to WordData("మూడు", "Moodu"),
        4 to WordData("నాలుగు", "Naalugu"),
        5 to WordData("ఐదు", "Aidu"),
        6 to WordData("ఆరు", "Aaru"),
        7 to WordData("ఏడు", "Eedu"),
        8 to WordData("ఎనిమిది", "Enimidi"),
        9 to WordData("తొమ్మిది", "Tommidi"),
        10 to WordData("పది", "Padi"),
        20 to WordData("ఇరవై", "Iravai"),
        24 to WordData("ఇరవై నాలుగు", "Iravai Naalugu"),
        25 to WordData("ఇరవై ఐదు", "Iravai Aidu"),
        30 to WordData("ముప్పై", "Muppai"),
        40 to WordData("నలభై", "Nalabhai"),
        50 to WordData("యాభై", "Yaabhai"),
        55 to WordData("యాభై ఐదు", "Yaabhai Aidu"),
        57 to WordData("యాభై ఏడు", "Yaabhai Eedu"),
        60 to WordData("అరవై", "Aravai"),
        70 to WordData("డెబ్బై", "Debbai"),
        78 to WordData("డెబ్బై ఎనిమిది", "Debbai Enimidi"),
        80 to WordData("ఎనభై", "Enabhai"),
        90 to WordData("తొంభై", "Tombhai"),
        99 to WordData("తొంభై తొమ్మిది", "Tombhai Tommidi"),
        100 to WordData("వంద", "Vanda")
    )

    // Kannada 0-100 mapping
    val kannadaWords = mapOf(
        0 to WordData("ಸೊನ್ನೆ", "Sonne"),
        1 to WordData("ಒಂದು", "Ondu"),
        2 to WordData("ಎರಡು", "Eradu"),
        3 to WordData("ಮೂರು", "Mooru"),
        4 to WordData("ನಾಲ್ಕು", "Naalku"),
        5 to WordData("ಐದು", "Aidu"),
        6 to WordData("ಆರು", "Aaru"),
        7 to WordData("ಏಳು", "Eelu"),
        8 to WordData("ಎಂಟು", "Entu"),
        9 to WordData("ಒಂಬತ್ತು", "Ombattu"),
        10 to WordData("ಹತ್ತು", "Hattu"),
        20 to WordData("ಇಪ್ಪತ್ತು", "Ippattu"),
        24 to WordData("ಇಪ್ಪತ್ನಾಲ್ಕು", "Ippatnaalku"),
        25 to WordData("ಇಪ್ಪತ್ತೈದು", "Ippattaidu"),
        30 to WordData("ಮೂವತ್ತು", "Moovattu"),
        40 to WordData("ನಲವತ್ತು", "Nalavattu"),
        50 to WordData("ಐವತ್ತು", "Aivattu"),
        55 to WordData("ಐವತ್ತೈದು", "Aivattaidu"),
        57 to WordData("ಐವತ್ತೇಳು", "Aivatteelu"),
        60 to WordData("ಅರವತ್ತು", "Aravattu"),
        70 to WordData("ಎಪ್ಪತ್ತು", "Eppattu"),
        78 to WordData("ಎಪ್ಪತ್ತೆಂಟು", "Eppattentu"),
        80 to WordData("ಎಂಬತ್ತು", "Embattu"),
        90 to WordData("ತೊಂಬತ್ತು", "Tombattu"),
        99 to WordData("ತೊಂಬತ್ತೊಂಬತ್ತು", "Tombattonbattu"),
        100 to WordData("ನೂರು", "Nooru")
    )

    // Malayalam 0-100 mapping
    val malayalamWords = mapOf(
        0 to WordData("പൂജ്യം", "Poojyam"),
        1 to WordData("ഒന്ന്", "Onnu"),
        2 to WordData("രണ്ട്", "Randu"),
        3 to WordData("മൂന്ന്", "Moonnu"),
        4 to WordData("നാല്", "Naalu"),
        5 to WordData("അഞ്ച്", "Anchu"),
        6 to WordData("ആറ്", "Aaru"),
        7 to WordData("ഏഴ്", "Ezhu"),
        8 to WordData("എട്ട്", "Ettu"),
        9 to WordData("ഒൻപത്", "Onpathu"),
        10 to WordData("പത്ത്", "Pathu"),
        20 to WordData("ഇരുപത്", "Irupathu"),
        24 to WordData("ഇരുപത്തിനാല്", "Irupathinaalu"),
        25 to WordData("ഇരുപത്തിയഞ്ച്", "Irupathiyanchu"),
        30 to WordData("മുപ്പത്", "Muppathu"),
        40 to WordData("നാൽപ്പത്", "Naalpathu"),
        50 to WordData("അമ്പത്", "Ampathu"),
        55 to WordData("അമ്പത്തിയഞ്ച്", "Ambathiyanchu"),
        57 to WordData("അമ്പത്തിയേഴ്", "Ambathiyezhu"),
        60 to WordData("അറുപത്", "Arupathu"),
        70 to WordData("എഴുപത്", "Ezhupathu"),
        78 to WordData("എഴുപത്തിയെട്ട്", "Ezhupathiyettu"),
        80 to WordData("എൺപത്", "Enpathu"),
        90 to WordData("തൊണ്ണൂറ്", "Thonnooru"),
        99 to WordData("തൊണ്ണൂറ്റിയൊമ്പത്", "Thonnootti-onpathu"),
        100 to WordData("നൂറ്", "Nooru")
    )

    // Punjabi 0-100 mapping
    val punjabiWords = mapOf(
        0 to WordData("ਸਿਫ਼ਰ", "Sifar"),
        1 to WordData("ਇੱਕ", "Ikk"),
        2 to WordData("ਦੋ", "Do"),
        3 to WordData("ਤਿੰਨ", "Tinn"),
        4 to WordData("ਚਾਰ", "Chaar"),
        5 to WordData("ਪੰਜ", "Panj"),
        6 to WordData("ਛੇ", "Chhe"),
        7 to WordData("ਸੱਤ", "Satt"),
        8 to WordData("ਅੱਠ", "Atth"),
        9 to WordData("ਨੌਂ", "Nau"),
        10 to WordData("ਦਸ", "Das"),
        20 to WordData("ਵੀਹ", "Veeh"),
        24 to WordData("ਚੌਵੀ", "Chauvi"),
        25 to WordData("ਪੰੱਚੀ", "Panchhi"),
        30 to WordData("ਤੀਹ", "Teeh"),
        40 to WordData("ਚਾਲੀ", "Chaali"),
        50 to WordData("ਪੰਜਾਹ", "Panjah"),
        55 to WordData("ਪਚਵੰਜਾ", "Pachvanja"),
        57 to WordData("ਸਤਾਵੰਜਾ", "Satavanja"),
        60 to WordData("ਸੱਠ", "Satth"),
        70 to WordData("ਸੱਤਰ", "Sattar"),
        78 to WordData("ਅਠੱਤਰ", "Athhattar"),
        80 to WordData("ਅੱਸੀ", "Assi"),
        90 to WordData("ਨੱਬੇ", "Nabbi"),
        99 to WordData("ਨੜਿੰਨਵੇ", "Narinnve"),
        100 to WordData("ਸੌ", "Sau")
    )

    // Odia 0-100 mapping
    val odiaWords = mapOf(
        0 to WordData("ଶୂନ", "Shuna"),
        1 to WordData("ଏକ", "Eka"),
        2 to WordData("ଦୁଇ", "Dui"),
        3 to WordData("ତିନି", "Tini"),
        4 to WordData("ଚାରି", "Chaari"),
        5 to WordData("ପାଞ୍ଚ", "Paancha"),
        6 to WordData("ଛଅ", "Chhaa"),
        7 to WordData("ସାତ", "Saata"),
        8 to WordData("ଆଠ", "Aatha"),
        9 to WordData("ନଅ", "Naa"),
        10 to WordData("ଦଶ", "Dasha"),
        20 to WordData("କୋଡ଼ିଏ", "Kodie"),
        24 to WordData("ଚବିଶ", "Chabisha"),
        25 to WordData("ପଚିଶ", "Pachisha"),
        30 to WordData("ତିରିଶ", "Tirisha"),
        40 to WordData("ଚାଳିଶ", "Chaalisha"),
        50 to WordData("ପଚାଶ", "Pachaasha"),
        55 to WordData("ପଞ୍ଚାବନ", "Panchabana"),
        57 to WordData("ସତାବନ", "Satabana"),
        60 to WordData("ଷାଠିଏ", "Saathie"),
        70 to WordData("ସତୁରି", "Saturi"),
        78 to WordData("ଅଠସ୍ତରି", "Athastari"),
        80 to WordData("ଅଶୀ", "Ashi"),
        90 to WordData("ନବେ", "Nabe"),
        99 to WordData("ଅନେଶତ", "Aneshata"),
        100 to WordData("ଶହ", "Shaha")
    )

    fun getWordData(langCode: String, number: Int): WordData? {
        return when (langCode.lowercase()) {
            "hi" -> hindiWords[number]
            "mr" -> marathiWords[number] ?: hindiWords[number]
            "gu" -> gujaratiWords[number] ?: hindiWords[number]
            "bn" -> bengaliWords[number] ?: hindiWords[number]
            "ta" -> tamilWords[number] ?: englishWords[number]
            "te" -> teluguWords[number] ?: englishWords[number]
            "kn" -> kannadaWords[number] ?: englishWords[number]
            "ml" -> malayalamWords[number] ?: englishWords[number]
            "pa" -> punjabiWords[number] ?: hindiWords[number]
            "or" -> odiaWords[number] ?: hindiWords[number]
            "en" -> englishWords[number]
            else -> hindiWords[number] ?: englishWords[number]
        }
    }

    fun getEnglishWord(number: Int): WordData {
        return englishWords[number] ?: WordData("$number", "$number")
    }

    // High Indian scale units
    fun getScaleWord(langCode: String, scale: Long): WordData {
        return when (scale) {
            100L -> when (langCode.lowercase()) {
                "hi" -> WordData("सौ", "Sau")
                "mr" -> WordData("शे", "She")
                "gu" -> WordData("સો", "So")
                "bn" -> WordData("শত", "Shoto")
                "ta" -> WordData("நூறு", "Nooru")
                "te" -> WordData("వంద", "Vanda")
                "kn" -> WordData("ನೂರು", "Nooru")
                "ml" -> WordData("നൂറ്", "Nooru")
                "pa" -> WordData("ਸੌ", "Sau")
                "or" -> WordData("ଶହ", "Shaha")
                else -> WordData("Hundred", "Hundred")
            }
            1000L -> when (langCode.lowercase()) {
                "hi" -> WordData("हज़ार", "Hazaar")
                "mr" -> WordData("हजार", "Hajar")
                "gu" -> WordData("હજાર", "Hajar")
                "bn" -> WordData("হাজার", "Hajar")
                "ta" -> WordData("ஆயிரம்", "Aayiram")
                "te" -> WordData("వేలు", "Velu")
                "kn" -> WordData("ಸಾವಿರ", "Saavira")
                "ml" -> WordData("ആയിരം", "Aayiram")
                "pa" -> WordData("ਹਜ਼ਾਰ", "Hazaar")
                "or" -> WordData("ହଜାର", "Hajara")
                else -> WordData("Thousand", "Thousand")
            }
            100000L -> when (langCode.lowercase()) {
                "hi" -> WordData("लाख", "Lakh")
                "mr" -> WordData("लाख", "Lakh")
                "gu" -> WordData("લાખ", "Lakh")
                "bn" -> WordData("লাখ", "Lakh")
                "ta" -> WordData("லட்சம்", "Latcham")
                "te" -> WordData("లక్ష", "Laksha")
                "kn" -> WordData("ಲಕ್ಷ", "Laksha")
                "ml" -> WordData("ലക്ഷം", "Laksham")
                "pa" -> WordData("ਲੱਖ", "Lakh")
                "or" -> WordData("ଲକ୍ଷ", "Lakhya")
                else -> WordData("Lakh", "Lakh")
            }
            10000000L -> when (langCode.lowercase()) {
                "hi" -> WordData("करोड़", "Crore")
                "mr" -> WordData("कोटी", "Koti")
                "gu" -> WordData("કરોડ", "Crore")
                "bn" -> WordData("কোটি", "Koti")
                "ta" -> WordData("கோடி", "Kodi")
                "te" -> WordData("కోటి", "Koti")
                "kn" -> WordData("ಕೋಟಿ", "Koti")
                "ml" -> WordData("കോടി", "Kodi")
                "pa" -> WordData("ਕਰੋੜ", "Crore")
                "or" -> WordData("କୋଟି", "Koti")
                else -> WordData("Crore", "Crore")
            }
            else -> WordData("", "")
        }
    }

    // Currency terms
    fun getCurrencyWord(langCode: String, symbol: String): WordData {
        if (symbol.isEmpty() || symbol == "") return WordData("", "")
        return when (langCode.lowercase()) {
            "hi" -> WordData("रुपये", "Rupaye")
            "mr" -> WordData("रुपये", "Rupaye")
            "gu" -> WordData("રૂપિયા", "Rupiya")
            "bn" -> WordData("টাকা", "Taka")
            "ta" -> WordData("ரூபாய்", "Roobai")
            "te" -> WordData("రూపాయలు", "Roopayalu")
            "kn" -> WordData("ರೂಪಾಯಿ", "Roopaayi")
            "ml" -> WordData("രൂപ", "Roopa")
            "pa" -> WordData("ਰੁਪਏ", "Rupaye")
            "or" -> WordData("ଟଙ୍କା", "Tanka")
            else -> WordData("Rupees", "Rupees")
        }
    }
}
