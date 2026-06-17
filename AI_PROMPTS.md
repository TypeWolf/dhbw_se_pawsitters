# Nutzung von Künstlicher Intelligenz (KI) - Pawsitters

In diesem Projekt wurde Künstliche Intelligenz (KI) strategisch als unterstützendes Werkzeug in verschiedenen Phasen des Software-Lebenszyklus eingesetzt. Dieses Dokument beschreibt detailliert, wie wir KI-Technologien genutzt haben, welche Rollen sie spielten und wie wir die Qualität der generierten Ergebnisse sichergestellt haben.

## 1. Strategische Einsatzgebiete

### 1.1 Code-Generierung und Architektur
KI wurde als "Sparringspartner" für die Erstellung der initialen Systemarchitektur genutzt. Wir haben KI-Modelle verwendet, um Vorschläge für die Schichtenarchitektur (Layered Architecture) und das **Unit of Work Pattern** zu generieren. Jedoch haben immer wir uns schlussendlich für die Architektur und das UnitOfWork entschieden. Bei der eigentlichen Implementierung half die KI beim Schreiben von Boilerplate-Code, Repositories und Service-Strukturen in Spring Boot. Dies beschleunigte den Entwicklungsprozess, indem repetitive Aufgaben automatisiert wurden.

### 1.2 Code-Analyse und Refactoring
Bestehender Code wurde regelmäßig durch KI-Modelle analysiert, um potenzielle Schwachstellen, Code-Smells oder Ineffizienzen zu identifizieren. Insbesondere beim Refactoring des Persistenz-Layers gab die KI wertvolle Hinweise zur Optimierung von JPA-Abfragen und zur Vermeidung des N+1-Problems.

### 1.3 Dokumentationserstellung
Große Teile der technischen Dokumentation, einschließlich der `ARCHITECTURE.md` und des `SECURITY_CONCEPT.md`, wurden mithilfe von KI entworfen. Die KI half dabei, komplexe technische Sachverhalte klar zu strukturieren und in präzises Deutsch bzw. Englisch zu übersetzen. Auch die Erstellung von **Mermaid-Sequenzdiagrammen** wurde durch KI-gestützte Entwürfe unterstützt.

### 1.4 Wissensaufbau und Technologie-Einstieg
Da das Projekt auf modernen Stacks wie Spring Boot 3.4.1 und Playwright basiert, nutzten wir die KI als interaktive Lernressource. Sie half uns, spezifische Framework-Details schneller zu verstehen, Best Practices für die API-Gestaltung zu verinnerlichen und fundierte Design-Entscheidungen im Frontend abzuwägen.

---

## 2. Qualitätssicherung: "Human-in-the-Loop"

Trotz der intensiven Nutzung von KI-Tools galt im gesamten Projekt das Prinzip der **menschlichen Letztentscheidung**.

- **Kritische Überprüfung**: Jedes von der KI generierte Code-Fragment oder Dokumentationsstück wurde von mindestens einem Teammitglied (Luis, Marios oder Willi) akribisch gelesen und auf Korrektheit, Sicherheit und Übereinstimmung mit den Projektkonventionen geprüft.
- **Hohe Ablehnungsrate**: Es ist wichtig zu betonen, dass eine beträchtliche Menge der KI-Vorschläge **abgelehnt** wurde. Oft entsprachen die Vorschläge nicht unseren Qualitätsansprüchen, waren zu generisch oder berücksichtigten spezifische Sicherheitsaspekte nicht ausreichend. Wir haben KI-Outputs nie blind übernommen, sondern sie als Rohmaterial betrachtet, das oft massiv manuell nachbearbeitet werden musste.
- **Sicherheitsfokus**: KI neigt dazu, funktionierenden, aber unsicheren Code zu schreiben. Hier haben wir manuell eingegriffen, um sicherzustellen, dass beispielsweise CORS-Regeln, Passwort-Hashing und die Transaktionsverwaltung den realen Sicherheitsanforderungen entsprechen.

---

## 3. Erweiterung der Testabdeckung

Ein wesentlicher Vorteil des KI-Einsatzes war die Skalierung unserer Test-Suite:

- **Generierung von Test-Cases**: Basierend auf den von uns manuell geschriebenen Kern-Tests (Sorgfaltstests für kritische Logik), haben wir die KI genutzt, um weitere Randfälle (Edge Cases) und Variationen von Tests zu generieren.
- **E2E-Tests mit Playwright**: Die KI half dabei, repetitive Selektoren-Abläufe für Playwright-Tests zu entwerfen, was uns erlaubte, eine deutlich höhere Testabdeckung für verschiedene Browser-Szenarien zu erreichen, als es rein manuell in der vorgegebenen Zeit möglich gewesen wäre.
- **Validierung**: Auch hier wurden alle KI-generierten Tests daraufhin geprüft, ob sie tatsächlich sinnvolle Assertions enthalten oder lediglich "grüne Haken" ohne echte Aussagekraft produzieren.

---

## 4. Fazit

Die Nutzung von KI in Pawsitters war kein Selbstzweck, sondern ein Werkzeug zur Effizienzsteigerung und Qualitätssicherung. Durch den bewussten Umgang mit den Limitationen der Technologie und die konsequente menschliche Kontrolle konnten wir die Vorteile der KI nutzen, ohne die Integrität und Qualität der Software zu gefährden. Das Team behielt zu jedem Zeitpunkt die volle Kontrolle über den Code und die Architektur.

---
*Dokumentationsstand: Juni 2026*
*Erstellt durch das Pawsitters-Entwicklerteam*
