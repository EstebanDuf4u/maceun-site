package fr.maceun.api.release;

import java.util.List;

public record ReleasesResponse(Release latest, List<Release> previous) {}
