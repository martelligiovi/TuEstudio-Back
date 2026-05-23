INSERT INTO catalog_universities (id, name, logo) VALUES
    ('uba', 'UBA', '🎓'),
    ('utn', 'UTN', '🔧'),
    ('unlam', 'UNLAM', '📚'),
    ('unc', 'UNC', '🏛️'),
    ('unq', 'UNQ', '💡')
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_careers (id, name, university_id) VALUES
    ('ingenieria-informatica', 'Ingeniería Informática', 'utn'),
    ('licenciatura-sistemas', 'Lic. en Sistemas', 'uba'),
    ('ingenieria-civil', 'Ingeniería Civil', 'uba'),
    ('medicina', 'Medicina', 'uba'),
    ('ingenieria-electronica', 'Ingeniería Electrónica', 'utn'),
    ('ciencias-economicas', 'Ciencias Económicas', 'uba'),
    ('exactas', 'Cs. Exactas y Naturales', 'uba')
ON CONFLICT (id) DO NOTHING;

INSERT INTO catalog_subjects (id, name, icon) VALUES
    ('analisis-matematico', 'Análisis Matemático', '📐'),
    ('algebra', 'Álgebra Lineal', '🔢'),
    ('fisica-i', 'Física I', '⚡'),
    ('fisica-ii', 'Física II', '🔭'),
    ('quimica-general', 'Química General', '🧪'),
    ('programacion-i', 'Programación I', '💻'),
    ('bases-datos', 'Bases de Datos', '🗄️'),
    ('java', 'Java', '☕'),
    ('estadistica', 'Estadística', '📊'),
    ('termodinamica', 'Termodinámica', '🌡️')
ON CONFLICT (id) DO NOTHING;
