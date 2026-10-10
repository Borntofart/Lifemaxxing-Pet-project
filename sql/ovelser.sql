-- Øvelser til programmet. Kør filen mod lifemaxxing databasen.
-- type styrer sæt og reps i WorkoutService, dosering bruges kun til cardio og HIIT.
-- Rækkefølgen (ovelse_id) er prioriteten: den første øvelse i en muskelgruppe vælges først.

ALTER TABLE ovelse ADD COLUMN IF NOT EXISTS type VARCHAR(20);
ALTER TABLE ovelse ADD COLUMN IF NOT EXISTS dosering VARCHAR(50);

INSERT INTO ovelse (ovelse_id, navn, muskelgruppe, type, dosering, beskrivelse) VALUES
-- Bryst
(1,  'Bænkpres',                   'Bryst',      'COMPOUND',  NULL, 'Basisøvelsen for bryst, forreste skulder og triceps. Nem at progressere med vægt over tid.'),
(2,  'Incline Dumbbell Press',     'Bryst',      'COMPOUND',  NULL, 'Skrå bænk på ca. 30 grader rammer den øverste del af brystet mere end flad bænk.'),
(3,  'Dips',                       'Bryst',      'COMPOUND',  NULL, 'Læn overkroppen frem for at ramme bryst mere. Giver et stort stræk i bunden.'),
(4,  'Incline Bænkpres',           'Bryst',      'COMPOUND',  NULL, 'Stang-variant af skrå pres. Godt alternativ når dumbbells bliver for tunge.'),
(5,  'Dumbbell Bænkpres',          'Bryst',      'COMPOUND',  NULL, 'Større bevægelsesudslag end med stang og hver side arbejder for sig selv.'),
(6,  'Maskine brystpres',          'Bryst',      'COMPOUND',  NULL, 'Stabil bevægelse, så man kan træne tæt på failure uden spotter.'),
(7,  'Vægtede push-ups',           'Bryst',      'COMPOUND',  NULL, 'Giver samme muskelvækst som bænkpres ved samme belastning. Brug vægtvest eller skive på ryggen.'),
(8,  'Kabel flyes',                'Bryst',      'ISOLATION', NULL, 'Kablerne holder spændingen hele vejen igennem bevægelsen.'),
(9,  'Pec deck',                   'Bryst',      'ISOLATION', NULL, 'Maskine-flyes. Fokus på et langsomt stræk i bunden.'),
(10, 'Dumbbell flyes',             'Bryst',      'ISOLATION', NULL, 'Mest belastning i den strakte position, hvor brystet er længst.'),

-- Skuldre
(11, 'Skulderpres',                'Skuldre',    'COMPOUND',  NULL, 'Stående pres over hovedet med stang. Rammer primært forreste skulder og triceps.'),
(12, 'Siddende dumbbell skulderpres', 'Skuldre', 'COMPOUND',  NULL, 'Ryglænet giver stabilitet, så skuldrene kan tage mere af arbejdet.'),
(13, 'Landmine press',             'Skuldre',    'COMPOUND',  NULL, 'Skrå presvinkel der ofte er mere skånsom for skuldre end pres lige over hovedet.'),
(14, 'Sidehævninger',              'Skuldre',    'ISOLATION', NULL, 'Den vigtigste øvelse for siden af skulderen, som giver bredde. Lette vægte, mange reps.'),
(15, 'Face Pulls',                 'Bagskulder', 'ISOLATION', NULL, 'Bagerste skulder og rotatorcuff. Godt modstykke til meget pres.'),
(16, 'Kabel sidehævninger',        'Skuldre',    'ISOLATION', NULL, 'Kablet giver modstand i bunden af bevægelsen, hvor dumbbells er lettest.'),
(17, 'Reverse pec deck',           'Bagskulder', 'ISOLATION', NULL, 'Isolerer bagerste skulder. Hold armene næsten strakte.'),

-- Triceps
(18, 'Close-grip bænkpres',        'Triceps',    'COMPOUND',  NULL, 'Smalt greb flytter arbejdet fra bryst over på triceps.'),
(19, 'Diamant push-ups',           'Triceps',    'COMPOUND',  NULL, 'Kropsvægtsvariant med hænderne tæt sammen.'),
(20, 'Overhead triceps extension', 'Triceps',    'ISOLATION', NULL, 'Med armene over hovedet bliver det lange triceps-hoved strakt, og det giver mere vækst end pushdowns.'),
(21, 'Triceps Pushdown',           'Triceps',    'ISOLATION', NULL, 'Simpel kabeløvelse der er nem at progressere på.'),
(22, 'Skull crushers',             'Triceps',    'ISOLATION', NULL, 'Sænk stangen bag hovedet for et større stræk.'),

-- Ryg
(23, 'Pull-ups',                   'Ryg',        'COMPOUND',  NULL, 'Den bedste kropsvægtsøvelse for lats. Brug elastik eller maskine hvis du ikke kan tage dem endnu.'),
(24, 'Bøjet roning',               'Ryg',        'COMPOUND',  NULL, 'Rammer hele den øvre ryg. Hold ryggen neutral og overkroppen stille.'),
(25, 'Lat pulldown',               'Ryg',        'COMPOUND',  NULL, 'Samme bevægelse som pull-ups, men vægten kan justeres præcist.'),
(26, 'Brystunderstøttet roning',   'Ryg',        'COMPOUND',  NULL, 'Brystet ligger mod bænken, så lænden ikke bliver træt før ryggen.'),
(27, 'Kabelroning',                'Ryg',        'COMPOUND',  NULL, 'Siddende roning med konstant spænding fra kablet.'),
(28, 'Enarmet dumbbell roning',    'Ryg',        'COMPOUND',  NULL, 'Træner en side ad gangen og giver et langt bevægelsesudslag.'),
(29, 'Chin-ups',                   'Ryg',        'COMPOUND',  NULL, 'Underhåndsgreb giver mere biceps end pull-ups.'),
(30, 'Straight-arm pulldown',      'Ryg',        'ISOLATION', NULL, 'Isolerer lats uden at biceps hjælper til.'),
(31, 'Dumbbell pullover',          'Ryg',        'ISOLATION', NULL, 'Giver et stort stræk på lats over hovedet.'),
(32, 'Shrugs',                     'Ryg',        'ISOLATION', NULL, 'Øvre trapezius. Hold toppen et sekund.'),

-- Biceps
(33, 'Biceps Curls',               'Biceps',     'ISOLATION', NULL, 'Klassisk curl med stang. Nem at progressere på.'),
(34, 'Incline dumbbell curls',     'Biceps',     'ISOLATION', NULL, 'På skrå bænk er biceps strakt i bunden, og det peger studier på giver god vækst.'),
(35, 'Hammer curls',               'Biceps',     'ISOLATION', NULL, 'Neutralt greb rammer brachialis og underarm mere.'),
(36, 'Preacher curls',             'Biceps',     'ISOLATION', NULL, 'Armen ligger fast, så der ikke kan snydes med kroppen.'),
(37, 'Kabel curls',                'Biceps',     'ISOLATION', NULL, 'Konstant spænding hele vejen op og ned.'),

-- Quadriceps
(38, 'Squat',                      'Quadriceps', 'COMPOUND',  NULL, 'Basisøvelsen for ben. Gå så dybt du kan med god teknik, dybe squats giver mere vækst end halve.'),
(39, 'Benpres',                    'Quadriceps', 'COMPOUND',  NULL, 'Meget belastning på ben uden at ryggen bliver begrænsningen.'),
(40, 'Hack squat',                 'Quadriceps', 'COMPOUND',  NULL, 'Maskine-squat med fokus på forsiden af lårene.'),
(41, 'Bulgarian Split Squat',      'Quadriceps', 'COMPOUND',  NULL, 'Etbens-øvelse der rammer quads og balder og retter skævheder mellem siderne.'),
(42, 'Front squat',                'Quadriceps', 'COMPOUND',  NULL, 'Vægten foran gør overkroppen mere oprejst og flytter arbejdet over på quads.'),
(43, 'Walking Lunges',             'Quadriceps', 'COMPOUND',  NULL, 'Godt til både ben og balance. Lange skridt rammer balder mere.'),
(44, 'Goblet squat',               'Quadriceps', 'COMPOUND',  NULL, 'Den letteste måde at lære at squatte på.'),
(45, 'Leg extension',              'Quadriceps', 'ISOLATION', NULL, 'Den eneste øvelse der rammer rectus femoris ordentligt i forkortet position.'),
(46, 'Reverse Nordic',             'Quadriceps', 'ISOLATION', NULL, 'Kropsvægt-øvelse der giver et stort stræk på forsiden af låret.'),

-- Baglår
(47, 'Rumænsk dødløft',            'Baglår',     'COMPOUND',  NULL, 'Træner baglår og balder i strakt position. Skub hoften bagud og hold knæene let bøjede.'),
(48, 'Dødløft',                    'Baglår',     'COMPOUND',  NULL, 'Træner hele bagsiden af kroppen. Den øvelse hvor man kan løfte mest vægt.'),
(49, 'Good mornings',              'Baglår',     'COMPOUND',  NULL, 'Hoftebøj med stangen på ryggen. Start let.'),
(50, 'Etbens rumænsk dødløft',     'Baglår',     'COMPOUND',  NULL, 'Træner en side ad gangen og udfordrer balancen.'),
(51, 'Siddende leg curl',          'Baglår',     'ISOLATION', NULL, 'Giver mere vækst i baglåret end liggende leg curl, fordi musklen er mere strakt når man sidder.'),
(52, 'Leg Curl',                   'Baglår',     'ISOLATION', NULL, 'Liggende leg curl. Godt supplement til de tunge hoftebøj-øvelser.'),
(53, 'Nordic hamstring curl',      'Baglår',     'ISOLATION', NULL, 'Studier viser at øvelsen halverer risikoen for baglårsskader.'),

-- Balder
(54, 'Hip thrust',                 'Balder',     'COMPOUND',  NULL, 'Meget høj aktivering af balderne og nem at belaste tungt.'),
(55, 'Step-ups',                   'Balder',     'COMPOUND',  NULL, 'Høj boks og læn let frem for at ramme balderne mere.'),
(56, 'Kabel pull-through',         'Balder',     'COMPOUND',  NULL, 'Lær hoftebøjet med lav belastning på lænden.'),
(57, 'Glute bridge',               'Balder',     'COMPOUND',  NULL, 'Hip thrust fra gulvet. God til begyndere.'),
(58, 'Kabel hip abduction',        'Balder',     'ISOLATION', NULL, 'Rammer den lille og mellemste ballemuskel på siden af hoften.'),

-- Læg
(59, 'Lægløft',                    'Læg',        'ISOLATION', NULL, 'Stående med strakte knæ. Rammer gastrocnemius. Hold strækket i bunden.'),
(60, 'Siddende lægløft',           'Læg',        'ISOLATION', NULL, 'Med bøjede knæ rammes soleus, som stående lægløft ikke træner så godt.'),
(61, 'Lægløft i benpres',          'Læg',        'ISOLATION', NULL, 'Godt alternativ hvis der ikke er en lægmaskine.'),

-- Core
(62, 'Planke',                     'Core',       'ISOLATION', NULL, 'Træner mavemusklerne i at holde ryggen stabil.'),
(63, 'Pallof press',               'Core',       'ISOLATION', NULL, 'Antirotation. Stå imod kablet der prøver at dreje kroppen.'),
(64, 'Hanging leg raise',          'Core',       'ISOLATION', NULL, 'Løft benene og rul bækkenet op for at ramme den nederste del af maven.'),
(65, 'Ab wheel rollout',           'Core',       'ISOLATION', NULL, 'Meget høj belastning på mavemusklerne. Start med korte udrulninger.'),
(66, 'Dead bug',                   'Core',       'ISOLATION', NULL, 'Skånsom øvelse med fokus på at holde lænden mod gulvet.'),
(67, 'Kabel crunch',               'Core',       'ISOLATION', NULL, 'Mavebøjninger med vægt, så de kan progresseres som andre øvelser.'),
(68, 'Sideplanke',                 'Core',       'ISOLATION', NULL, 'Træner de skrå mavemuskler og hoften.'),

-- Cardio
(69, 'Opvarmning, rolig jog',      'Opvarmning', 'CARDIO',    '10 min',     'Rolig start der hæver pulsen gradvist.'),
(70, 'Opvarmning, romaskine',      'Opvarmning', 'CARDIO',    '8 min',      'Varmer både over- og underkrop op.'),
(71, 'Intervalløb 4x4',            'Kondition',  'CARDIO',    '4 x 4 min',  '4 minutter på 85-95 % af maxpuls med 3 min pause. En af de mest effektive måder at øge kondital på.'),
(72, 'Romaskine',                  'Kondition',  'CARDIO',    '10 min',     'Skånsom for leddene og bruger det meste af kroppen.'),
(73, 'Cykel, zone 2',              'Kondition',  'CARDIO',    '20 min',     'Roligt tempo hvor man stadig kan snakke. Bygger grundkondition.'),
(74, 'Incline gang på løbebånd',   'Kondition',  'CARDIO',    '15 min',     'Høj forbrænding med lav belastning på knæene.'),
(75, 'Crosstrainer',               'Kondition',  'CARDIO',    '15 min',     'Skånsomt alternativ til løb.'),
(76, 'Svømning',                   'Kondition',  'CARDIO',    '20 min',     'Træner hele kroppen uden stød på leddene.'),
(77, 'Sprintintervaller på cykel', 'Kondition',  'CARDIO',    '8 x 30 sek', 'Korte sprints med 90 sek pause. Meget effekt på kort tid.'),
(78, 'Nedkøling og stræk',         'Nedkøling',  'CARDIO',    '5 min',      'Bring pulsen ned og stræk de muskler der er brugt.'),
(79, 'Rolig gang',                 'Nedkøling',  'CARDIO',    '5 min',      'Let gang til pulsen er nede igen.'),

-- HIIT
(80, 'Burpees',                    'Helkrop',    'HIIT',      '4 runder x 40 sek', 'Hele kroppen i en øvelse. Hæver pulsen meget hurtigt.'),
(81, 'Kettlebell Swings',          'Balder',     'HIIT',      '4 runder x 40 sek', 'Eksplosiv hoftebøj der træner balder, baglår og kondition på en gang.'),
(82, 'Mountain Climbers',          'Core',       'HIIT',      '4 runder x 40 sek', 'Core og kondition. Hold hoften i ro.'),
(83, 'Jump Squats',                'Quadriceps', 'HIIT',      '4 runder x 40 sek', 'Eksplosiv styrke i benene.'),
(84, 'Push-ups',                   'Bryst',      'HIIT',      '4 runder x 40 sek', 'Hurtige push-ups med god form.'),
(85, 'Box Jumps',                  'Quadriceps', 'HIIT',      '4 runder x 40 sek', 'Hop op og træd ned igen for at skåne akillessenen.'),
(86, 'Thrusters',                  'Helkrop',    'HIIT',      '4 runder x 40 sek', 'Front squat direkte over i skulderpres med lette dumbbells.'),
(87, 'Battle ropes',               'Helkrop',    'HIIT',      '4 runder x 30 sek', 'Overkrop og kondition uden stød på benene.'),
(88, 'Renegade rows',              'Ryg',        'HIIT',      '4 runder x 40 sek', 'Roning i plankeposition. Træner ryg og core sammen.')
ON CONFLICT (ovelse_id) DO UPDATE SET
    navn = EXCLUDED.navn,
    muskelgruppe = EXCLUDED.muskelgruppe,
    type = EXCLUDED.type,
    dosering = EXCLUDED.dosering,
    beskrivelse = EXCLUDED.beskrivelse;

ALTER TABLE ovelse ALTER COLUMN type SET NOT NULL;
