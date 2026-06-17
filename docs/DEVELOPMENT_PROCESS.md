# Detaillierter Entwicklungsprozess - Pawsitters

Dieses Dokument bietet einen umfassenden Einblick in die Entstehung von **Pawsitters**. Von der ersten Codezeile bis zur containerisierten Anwendung beschreibt dieser Bericht die methodische Vorgehensweise, die technologischen Entscheidungen und die synergetische Zusammenarbeit innerhalb des Entwicklerteams.

## Einleitung & Zielsetzung

Das Ziel des Projekts war die Entwicklung einer robusten, sicheren und benutzerfreundlichen Plattform für die Haustierbetreuung. Dabei standen nicht nur die funktionalen Anforderungen im Vordergrund, sondern auch moderne Software-Engineering-Prinzipien wie **Test-Driven Development (TDD)**, **CI/CD-Automatisierung** und eine klare **Schichtentrennung**.

---

## Verwendete Tools & Technologien

Um die gesteckten Ziele zu erreichen, wurde ein moderner Technologie-Stack gewählt:

- **Backend**: Java 17 mit Spring Boot 3.4.1 für eine performante API.
- **Datenbank**: H2 In-Memory für schnelle Iterationszyklen während der Entwicklung.
- **Frontend**: Vanilla JavaScript (ES6+), HTML5 und CSS3 zur Minimierung von Abhängigkeiten und maximalen Kontrolle.
- **Testing**: JUnit 5 für Unit-Tests und Playwright für umfassende E2E-Tests.
- **Infrastruktur**: GitHub Actions für die CI-Pipeline und Docker für die Containerisierung.
- **KI-Unterstützung**: Gemini wurde strategisch für Architekturvorschläge und Code-Reviews eingesetzt.

---

## Phasen der Entwicklung

### Phase 1: Architektur-Setup & Automatisierung (Mai 2026)
*Hauptverantwortlich: Luis*

Die erste Woche war der Grundsteinlegung gewidmet. Es war entscheidend, von Anfang an eine saubere Struktur zu haben:
- **Skeleton-Setup**: Luis erstellte mit Spring Boot das Grundgerüst. Dabei wurde besonderer Wert auf die Paketstruktur gelegt, um die spätere Modularisierung zu erleichtern.
- **Pipeline-First**: Noch bevor das erste Feature fertig war, wurde die GitHub Actions CI-Pipeline implementiert. Jeder Commit wurde fortan automatisch auf Kompilierbarkeit und Test-Erfolg geprüft.
- **Proof of Concept**: Implementierung einer rudimentären MVC-Struktur und einer Test-Seite, um das Zusammenspiel von Controller, Service und H2-Datenbank zu validieren.

### Phase 2: Design-Offensive & Feature-Implementierung (Mitte Mai 2026)
*Team: Marios (Lead Design), Luis (Lead Testing/Security), Willi (UI-Design-Support)*

Hier verlagerte sich der Schwerpunkt auf die Benutzererfahrung und die Kernlogik:
- **Design-System**: Marios entwarf ein konsistentes Design-System. Willi unterstützte hierbei bei der Konzeption der UI-Komponenten, um eine intuitive Bedienung sicherzustellen.
- **Frontend-Entwicklung**: Die statischen HTML-Seiten wurden durch dynamische JavaScript-Module ersetzt, die über die `api.js` mit dem Backend kommunizieren.
- **Playwright Integration**: Luis implementierte parallele E2E-Tests. Diese Tests simulierten echte Benutzerinteraktionen (Login, Haustier anlegen, Sitter-Anfrage erstellen) und wurden fester Bestandteil der CI-Pipeline.
- **Security-Härtung**: Einführung von BCrypt zur Passwort-Verschlüsselung und Implementierung von Validierungslogik in den Service-Schichten.

### Phase 3: Refactoring & Pattern-Implementierung (Ende Mai 2026)
*Hauptverantwortlich: Luis*

Mit wachsender Codebasis wurde die Komplexität im Datenzugriff zu einer Herausforderung:
- **Unit of Work Pattern**: Um die Transaktionskontrolle zu verbessern und Redundanzen in den Repositories zu vermeiden, implementierte Luis das Unit of Work Pattern. Dies führte zu einer deutlich saubereren Trennung von Business-Logik und Persistenz.
- **Test-Stabilität**: Die E2E-Tests wurden optimiert, um "Flakiness" zu vermeiden. Timeouts wurden angepasst und die Testdaten-Initialisierung wurde robuster gestaltet.

### Phase 4: Finalisierung, Chat & Docker (Juni 2026)
*Gemeinsame Entwicklung (Luis, Marios, Willi)*

Die letzte Phase konzentrierte sich auf den Feinschliff und die Deployment-Bereitschaft:
- **Echtzeit-Kommunikation**: Implementierung einer Chat-Funktion, die es Tierhaltern ermöglicht, direkt mit potenziellen Sittern in Kontakt zu treten.
- **Profil- & Adressmanagement**: Willi und Marios entwickelten das "My Account"-Fenster, inklusive der Logik zum Laden und Speichern von Adressdaten, was die Benutzerfreundlichkeit deutlich erhöhte.
- **Dockerisierung**: Das gesamte System wurde in Docker-Container verpackt. Die `docker-compose.yml` erlaubt nun den Start von Backend (Spring Boot) und Frontend (Nginx) in einer isolierten Umgebung.

---


## Methodik & Zusammenarbeit

- **Planung**: Wir arbeiteten mti einem Kanban-Board über GitHub Projects. Dieses half dabei, den Fortschritt unserer Tickets transparent den anderen Teammitgliedern offen zu zeigen. Zudem hatten wir Meetings, in denen der Stand und kommendes besprochen wurde. Allerdings waren diese unregelmäig. In diesen Terminen ging es um regelmäßigen Abstimmungen über den Fortschritt und die nächsten Prioritäten.
- **Code-Reviews**: Kritische Änderungen, insbesondere in der Security-Logik und im Persistenz-Layer, wurden gemeinsam gesichtet.
- **Iterative UI-Updates**: Das Design wurde basierend auf dem Feedback der E2E-Tests und manuellen "Reviews" kontinuierlich verfeinert.

## Fazit

Der Entwicklungsprozess von Pawsitters lief nicht perfekt ab. Trotzdem hat es uns gezeigt, was uns liegt, wie man in Zukunft besser plant.
Für die Zukunft müssen wir uns voher besser abstimmen, wer für etwas zuständig ist, um nicht immer dazwischen zu grätschen. Es sei denn, dass es sich anbietet, wie wir es zum Schluss hatten, dass eine Person sich um Backend und Frontend für ein Feature kümmert. Zudem würden wir vermehr pair Programming Sessions abhalten um bei Problemen schneller eingreifen zu können.

---


## Rollen zu beginn und Zum Schluss
### Beginn
| Name | Vorher |
| :--- | :---: |
| Luis Zipse | Backend |
| Marios Zoumpolakis | Frontend |
| Willi Baierle | Backend, Testing |

## Schluss
Wie schon erwähnt haben wir uns gegen Ende dazu entschieden, dass jeder ein Komplettes Feature einbaut. Virteil dabei war, dass das Backend und Frontend größtenteils stand und wir auch nur die neuen Feature einbauen konnten, ohne alles über den Haufen zu werfen. User Fortschritt ist schlussendlich dadurch deutlich schneller gewachsen.

---

*Dokumentationsstand: 17. Juni 2026*
*Erstellt durch das Pawsitters-Entwicklerteam*
