Um die Anforderungen an OWASP und Security Shift Left vollständig zu erfüllen, empfehle ich folgendes
  Vorgehen:

  Schritt 1: Behebung der "Access Control" Schwachstellen (OWASP Fokus)
   1. JWT-Authentifizierung einführen: Ersetze den Mock-Login durch eine echte tokenbasierte
      Authentifizierung. Der Server signiert ein JWT, das das Frontend bei jedem Request im
      Authorization-Header mitschickt.
   2. Spring Security härten:
       * Ändere SecurityConfig.java, um Requests standardmäßig zu verweigern
         (anyRequest().authenticated()).
       * Implementiere Role-Based Access Control (RBAC) direkt in den Endpunkten (z.B. mit
         @PreAuthorize("hasRole('ADMIN')")).
       * Entferne requesterId: Die Identität des Nutzers muss serverseitig aus dem Security-Kontext
         (Token) ermittelt werden, nicht über Parameter.
   3. CORS einschränken: Ersetze * durch die tatsächliche URL des Frontends (z.B. http://localhost:5500).

  Schritt 2: Integration von "Security Shift Left"
   1. Automatisierte Dependency-Checks: Füge das owasp-dependency-check Plugin in die pom.xml ein. Damit
      werden Bibliotheken in der CI-Pipeline automatisch auf bekannte Schwachstellen (CVEs) geprüft.
   2. Static Application Security Testing (SAST): Integriere ein Tool wie SonarQube oder Snyk in die
      GitHub Action (ci.yml), um Code-Schwachstellen (z.B. hardcoded Secrets) frühzeitig zu finden.
   3. Security Unit Tests: Schreibe Tests, die explizit versuchen, ohne Berechtigung auf Admin-Ressourcen
      zuzugreifen, um sicherzustellen, dass die Guards funktionieren.

  Schritt 3: Frontend-Härtung
   1. Sicherer Speicher: Überlege, sensible Daten nicht im localStorage (anfällig für XSS), sondern in
      HttpOnly Cookies zu speichern.
   2. Input Sanitization: Obwohl React vieles automatisch maskiert, sollten alle Benutzereingaben im
      Backend nochmals validiert werden (teilweise bereits in AppUserService vorhanden).