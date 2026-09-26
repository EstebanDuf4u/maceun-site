-- Retrait de l'onglet « Infos » et des dates manuelles : les dates viennent uniquement de Bandsintown.
DROP TABLE site_content;
DROP TABLE manual_event;

-- Les sons sont désormais récupérés depuis leur lien Fanlink (titre, pochette, plateformes)
-- et Deezer (date de sortie, genre). Les colonnes gardent la dernière valeur connue.
ALTER TABLE release DROP COLUMN subtitle;
ALTER TABLE release ALTER COLUMN title DROP NOT NULL;
ALTER TABLE release ALTER COLUMN genre DROP NOT NULL;
ALTER TABLE release ALTER COLUMN cover DROP NOT NULL;
ALTER TABLE release ALTER COLUMN release_date DROP NOT NULL;
ALTER TABLE release ADD COLUMN manual_release_date DATE;       -- pour un son pas encore sur Deezer
ALTER TABLE release ADD COLUMN isrc VARCHAR(20);
ALTER TABLE release ADD COLUMN platforms TEXT;                 -- JSON [{name, url}]
ALTER TABLE release ADD COLUMN synced_at TIMESTAMP;
ALTER TABLE release ADD COLUMN sync_error VARCHAR(300);
CREATE UNIQUE INDEX release_url_uk ON release (url);

-- Son à venir : on garde la date annoncée tant que Deezer ne le connaît pas
UPDATE release SET manual_release_date = release_date WHERE url LIKE '%hypeddit.com%';
