package fr.maceun.api.release;

import java.util.List;

public record ReleasesResponse(ReleaseView latest, List<ReleaseView> previous) {}
