-- Son épinglé : mis en avant sur le site à la place du plus récent (un seul à la fois).
ALTER TABLE release ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE;
