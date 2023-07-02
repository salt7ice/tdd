package com.example.core;

import com.example.core.exceptions.MapNotFoundException;
import com.example.core.usecase.CalculTrajectoire;
import data.MapsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCalculDistanceUT {
    final private boolean withHighways= false;
    final private boolean withoutHighways= true;
    final private boolean fastestPath = true;
    final private boolean shortestPath = false;
    final private int bigMap = 1;
    final private int smallMap = 2;

    /**
     * En tant qu'utilisateur, je souhaite partir dans la ville 'Avignon en provenance de la ville de Béziers.
     * Mais je souhaite avoir le trajet le plus court.
     *
     *                ┌─────────────┐
     *                │   arrivée   │
     *                └─────────────┘
     *                  │
     *                  │
     *                  ▼
     *                ┌─────────────┐
     *                │   Avignon   │ ─┐
     *                └─────────────┘  │
     *                  │              │
     *                  │ RN96 36km    │
     *                  │              │
     *                ┌─────────────┐  │
     *                │    Arles    │  │
     *                └─────────────┘  │
     *                  │              │
     *                  │ RN50 145km   │ RN84 80km
     *                  │              │
     * ┌────────┐     ┌─────────────┐  │
     * │ depart │ ──▶ │   Bézier    │  │
     * └────────┘     └─────────────┘  │
     *                  │              │
     *                  │ RN113 83km   │
     *                  │              │
     *                ┌─────────────┐  │
     *                │ Montpellier │ ─┘
     *                └─────────────┘
     *
     * Etape 1:
     * Commencer à partir du nœud de départ (Bézier) avec une distance de 0 et le marquer comme traité.
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │             - │ Oui    │
     * │ Montpellier │ Infinity │             - │ Non    │
     * │ Avignon     │ Infinity │             - │ Non    │
     * │ Arles       │ Infinity │             - │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 2:
     * Mettre à jour les distances des nœuds voisins de Bézier :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Non    │
     * │ Avignon     │ Infinity │ -             │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 3:
     * Sélectionner le nœud avec la distance minimale (Montpellier) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Oui    │
     * │ Avignon     │ 163      │ Montpellier   │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 4:
     * Sélectionner le nœud avec la distance minimale (Arles) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 5:
     * Sélectionner le nœud avec la distance minimale (Avignon) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Oui    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 6:
     * Sélectionner le chemin le plus court 163:
     * Bézier -> Montpellier (RN113)-> Avignon (RN84).
     *
     * Resultat:
     * le chemin le plus court
     * Bézier -> Montpellier á travers (RN113) -> Avignon á travers (RN84).
     */
    @Test
    public void should_return_the_shortest_trajectory_given_a_small_map() throws MapNotFoundException {
        CalculTrajectoire calculTrajectoire = new CalculTrajectoire(new MapsRepository());

        List expectedResult = Stream.of("Béziers", "RN113", "Montpellier", "RN84", "Avignon").collect(Collectors.toList());
        Assertions.assertThat(calculTrajectoire.execute(smallMap,"Béziers","Avignon", withHighways, shortestPath)).isEqualTo(expectedResult);
    }

    /**
     * En tant qu'utilisateur, je souhaite partir dans la ville 'Avignon en provenance de la ville de Bézier.
     * Mais je souhaite avoir le trajet le plus court et sans autoroutes.
     *
     *               ┌─────────────┐
     *               │   arrivée   │
     *               └─────────────┘
     *                 │
     *                 │
     *                 ▼
     *               ┌─────────────┐
     *   ┌────────── │   Avignon   │ ─┐
     *   │           └─────────────┘  │
     *   │             │              │
     *   │             │ RN96 36km    │
     *   │             │              │
     *   │           ┌─────────────┐  │
     *   │           │    Arles    │  │ A9 150km
     *   │           └─────────────┘  │
     *   │             │              │
     *   │ RN84 80km   │ RN50 145km   │
     *   │             │              │
     *   │           ┌─────────────┐  │
     *   │           │   Bézier    │ ─┘
     *   │           └─────────────┘
     *   │             │
     *   │             │ RN113 83km
     *   │             │
     *   │           ┌─────────────┐
     *   └────────── │ Montpellier │
     *               └─────────────┘
     *
     * Etape 1:
     * Commencer à partir du nœud de départ (Bézier) avec une distance de 0 et le marquer comme traité.
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │             - │ Oui    │
     * │ Montpellier │ Infinity │             - │ Non    │
     * │ Avignon     │ Infinity │             - │ Non    │
     * │ Arles       │ Infinity │             - │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 2:
     * Mettre à jour les distances des nœuds voisins de Bézier, Ignorer l'autoroute A9 (Bézier,Avignon):
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Non    │
     * │ Avignon     │ Infinity │ -             │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 3:
     * Sélectionner le nœud avec la distance minimale (Montpellier) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Oui    │
     * │ Avignon     │ 163      │ Montpellier   │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 4:
     * Sélectionner le nœud avec la distance minimale (Arles) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 5:
     * Sélectionner le nœud avec la distance minimale (Avignon) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Oui    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 6:
     * Sélectionner le chemin le plus court 163:
     * Bézier -> Montpellier (RN113)-> Avignon (RN84).
     *
     * Resultat:
     * le chemin le plus court
     * Bézier -> Montpellier (RN113) -> Avignon (RN84).
     */
    @Test
    public void should_return_the_shortest_distance_given_a_small_map_ignoring_highways() throws MapNotFoundException {
        CalculTrajectoire calculTrajectoire = new CalculTrajectoire(new MapsRepository());
        List expectedResult = Stream.of("Béziers", "RN113", "Montpellier", "RN84", "Avignon").collect(Collectors.toList());
        Assertions.assertThat(calculTrajectoire.execute(bigMap,"Béziers","Avignon", withoutHighways, shortestPath)).isEqualTo(expectedResult);
    }

    /**
     * En tant qu'utilisateur, je souhaite partir dans la ville 'Avignon en provenance de la ville de Béziers.
     * Mais je souhaite avoir le trajet le plus rapide et sans autoroutes.
     *
     *
     * ┌−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−┐
     * ╎                                              ╎
     * ╎                                              ╎
     * ╎               ┌─────────────┐                ╎
     * ╎   ┌────────── │   Avignon   │ ───┐           ╎
     * ╎   │           └─────────────┘    │           ╎
     * ╎   │             │                │           ╎
     * ╎   │             │ RN96 36km      │           ╎
     * ╎   │             │  100km/h       │           ╎
     * ╎   │           ┌─────────────┐    │           ╎
     * ╎   │ RN84 80km │    Arles    │    │ A9 150km  ╎
     * ╎   │   80km/h  └─────────────┘    │   130km/h ╎
     * ╎   │             │                │           ╎
     * ╎   │             │ RN50 145km     │           ╎
     * ╎   │             │  110km/h       │           ╎
     * ╎   │           ┌─────────────┐    │           ╎
     * ╎   │           │   Bézier    │ ───┘           ╎
     * ╎   │           └─────────────┘                ╎
     * ╎   │             │                            ╎
     * ╎   │             │                            ╎
     * ╎   │             │                            ╎
     * ╎   │             │ RN113 83km                 ╎
     * ╎   │             │    70km/h                  ╎
     * ╎   │           ┌─────────────┐                ╎
     * ╎   └────────── │ Montpellier │                ╎
     * ╎               └─────────────┘                ╎
     * ╎                                              ╎
     * └−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−-−−−−−−−−−−−−┘
     * Etape 1:
     * Commencer à partir du nœud de départ (Bézier) avec une distance de 0 et le marquer comme traité.
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │             - │ Oui    │
     * │ Montpellier │ Infinity │             - │ Non    │
     * │ Avignon     │ Infinity │             - │ Non    │
     * │ Arles       │ Infinity │             - │ Non    ││
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 2:
     * Mettre à jour les distances des nœuds voisins de Bézier, Ignorer l'autoroute A9 (Bézier,Avignon):
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Non    │
     * │ Avignon     │ Infinity │ -             │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 3:
     * Sélectionner le nœud avec la distance minimale (Montpellier) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Oui    │
     * │ Avignon     │ 163      │ Montpellier   │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 4:
     * Sélectionner le nœud avec la distance minimale (Arles) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 5:
     * Sélectionner le nœud avec la distance minimale (Avignon) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Oui    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 6:
     * Sélectionner le chemin le plus rapide 100.7:
     * Bézier -> Arles -> Avignon.
     *
     * Resultat: le chemin le plus rapide
     * Bézier -> Arles á travers RN50 -> Avignon á travers RN96.
     */
    @Test
    public void should_return_the_fastest_path_given_a_small_map_ignoring_highways() throws MapNotFoundException {
        CalculTrajectoire calculTrajectoire = new CalculTrajectoire(new MapsRepository());

        List expectedResult = Stream.of("Béziers", "RN50", "Arles", "RN96", "Avignon").collect(Collectors.toList());
        Assertions.assertThat(calculTrajectoire.execute(bigMap,"Béziers","Avignon",withoutHighways, fastestPath )).isEqualTo(expectedResult);
    }

    /**
     * En tant qu'utilisateur, je souhaite partir dans la ville 'Avignon en provenance de la ville de Béziers.
     * Mais je souhaite avoir le trajet le plus rapide.
     *
     *
     *
     * ┌−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−┐
     * ╎                                              ╎
     * ╎                                              ╎
     * ╎               ┌─────────────┐                ╎
     * ╎   ┌────────── │   Avignon   │ ───┐           ╎
     * ╎   │           └─────────────┘    │           ╎
     * ╎   │             │                │           ╎
     * ╎   │             │ RN96 36km      │           ╎
     * ╎   │             │  100km/h       │           ╎
     * ╎   │           ┌─────────────┐    │           ╎
     * ╎   │ RN84 80km │    Arles    │    │ A9 150km  ╎
     * ╎   │   80km/h  └─────────────┘    │   130km/h ╎
     * ╎   │             │                │           ╎
     * ╎   │             │ RN50 145km     │           ╎
     * ╎   │             │  110km/h       │           ╎
     * ╎   │           ┌─────────────┐    │           ╎
     * ╎   │           │   Bézier    │ ───┘           ╎
     * ╎   │           └─────────────┘                ╎
     * ╎   │                                          ╎
     * ╎   │                                          ╎
     * ╎   │             │                            ╎
     * ╎   │             │ RN113 83km                 ╎
     * ╎   │             │    70km/h                  ╎
     * ╎   │           ┌─────────────┐                ╎
     * ╎   └────────── │ Montpellier │                ╎
     * ╎               └─────────────┘                ╎
     * ╎                                              ╎
     * └−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−-−−−−−−−−−−−−−−┘
     *
     * Etape 1:
     * Commencer à partir du nœud de départ (Bézier) avec une distance de 0 et le marquer comme traité.
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │             - │ Oui    │
     * │ Montpellier │ Infinity │             - │ Non    │
     * │ Avignon     │ Infinity │             - │ Non    │
     * │ Arles       │ Infinity │             - │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 2:
     * Mettre à jour les distances des nœuds voisins de Bézier, Ignorer l'autoroute A9 (Bézier,Avignon):
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Non    │
     * │ Avignon     │ Infinity │ -             │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 3:
     * Sélectionner le nœud avec la distance minimale (Montpellier) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │ 0        │ -             │ Oui    │
     * │ Montpellier │ 83       │ Bézier        │ Oui    │
     * │ Avignon     │ 163      │ Montpellier   │ Non    │
     * │ Arles       │ 145      │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 4:
     * Sélectionner le nœud avec la distance minimale (Arles) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Non    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 5:
     * Sélectionner le nœud avec la distance minimale (Avignon) et mettre à jour les distances de ses nœuds voisins :
     * ┌─────────────┬──────────┬───────────────┬────────┐
     * │    Node     │ Distance │ Previous Node │ Traité │
     * ├─────────────┼──────────┼───────────────┼────────┤
     * │ Bézier      │        0 │ -             │ Oui    │
     * │ Montpellier │       83 │ Bézier        │ Oui    │
     * │ Avignon     │      163 │ Montpellier   │ Oui    │
     * │ Arles       │      145 │ Bézier        │ Oui    │
     * └─────────────┴──────────┴───────────────┴────────┘
     * Etape 6:
     * Sélectionner le chemin le plus rapide 69.2:
     * Bézier ->  Avignon.
     *
     * Resultat: le chemin le plus rapide
     * Bézier ->  Avignon á travers A9.
     */
    @Test
    public void should_return_the_fastest_path_given_a_small_map() throws MapNotFoundException {
        CalculTrajectoire calculTrajectoire = new CalculTrajectoire(new MapsRepository());

        //List expectedResult = Stream.of("Bézier", "A9", "Avignon").collect(Collectors.toList());
        List<String> expectedResult = List.of("Béziers", "A9", "Avignon");
        Assertions.assertThat(calculTrajectoire.execute(bigMap,"Béziers","Avignon",withHighways,fastestPath)).isEqualTo(expectedResult);
    }
}