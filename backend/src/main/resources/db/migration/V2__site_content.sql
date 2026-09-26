-- Tous les textes / liens / images du site, modifiables depuis l'admin (onglet « Infos »).
-- Un seul document JSON, validé côté Java (SiteContent).
CREATE TABLE site_content (
    id         INTEGER   PRIMARY KEY,
    data       TEXT      NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

INSERT INTO site_content (id, data, updated_at) VALUES (1, '{
  "hero": {
    "name": "MACEUN",
    "tagline": "DJ · Producer · Performer",
    "genres": "House — Techno — Général",
    "image": "/img/djm4.jpg"
  },
  "about": {
    "lead": "Maceun a commencé la musique en *2018*, à *9 ans*. Depuis, il crée ses propres morceaux et s''est récemment tourné vers la Techno avec *« Feel Your Love »*, un vrai tournant dans sa carrière.",
    "text": "Il a récemment joué à Paris, à La Boule Noire, avec le rappeur Rutra pour célébrer la sortie de son remix de *Salle Vide*. Il mixe aussi en festival et dans des événements privés : anniversaires, bals, soirées…",
    "image": "/img/djm6.jpg",
    "tags": ["House", "Techno", "Général", "Live Set", "Festival", "Bal"],
    "stats": [
      {"value": "2018", "label": "Premiers sons"},
      {"value": "4", "label": "Sorties Techno"},
      {"value": "1", "label": "Nouveau single"}
    ]
  },
  "contact": {
    "phone": "07 69 83 51 17",
    "bookingText": "Vous voulez booker Maceun pour votre événement ? Remplissez le formulaire, vous aurez une réponse au plus vite.",
    "streamingUrl": "https://maceun.fanlink.tv/",
    "socialImage": "/img/djm2.jpg"
  },
  "socials": [
    {"label": "Instagram", "url": "https://www.instagram.com/maceunofficiel"},
    {"label": "YouTube", "url": "https://www.youtube.com/@maceunofficiel"},
    {"label": "TikTok", "url": "https://www.tiktok.com/@maceunofficiel"},
    {"label": "Facebook", "url": "https://www.facebook.com/maceunofficiel"}
  ],
  "faq": [
    {"question": "Style de musique ?", "answer": "Général, House et Techno. Le set s''adapte au public et au type d''événement."},
    {"question": "Comment booker Maceun ?", "answer": "Via le formulaire ci-dessous ou par téléphone au 07 69 83 51 17. Merci de préciser la date, le lieu et le type d''événement."},
    {"question": "Quels événements ?", "answer": "Bals · Festivals · Soirées publiques & privées · Anniversaires · Et bien plus encore. Toutes les demandes sont étudiées."},
    {"question": "Où le suivre ?", "answer": "Instagram : https://www.instagram.com/maceunofficiel · YouTube : https://www.youtube.com/@maceunofficiel · TikTok : https://www.tiktok.com/@maceunofficiel"}
  ]
}', CURRENT_TIMESTAMP);
