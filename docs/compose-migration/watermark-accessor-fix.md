# Flag watermark JVM accessor clash

The supplied F-Droid Full compiler screenshot reports four diagnostics for two
conflicting JVM method signatures in `RpnFlagWatermarkState`:

- The delegated `flagText` property's setter and `setFlagText(String?)` both
  compile to `setFlagText(Ljava/lang/String;)V`.
- The delegated `spread` property's setter and `setSpread(Boolean)` both compile
  to `setSpread(Z)V`.

Private delegated properties still need accessors. Private visibility does not
make a second method with the same JVM signature legal. The old native View's
plain private fields did not expose the same delegated-property collision.

## Repair

Rename only the private backing identifiers to `flagTextValue` and
`spreadEnabled`, including their internal references. Preserve public
`setFlagText`, `setSpread`, `setFlagDrawable`, `clear`, and every call site.
No annotation-based JVM alias, suppression, UI redesign or logic change is used.

The focused source guard reverses exactly those two identifiers outside comments
and strings, then checks the entire file against its previous SHA256:
`b8188c257508fc4a52b5af91a69952d4cc7654604dfbf5877ae25fb40f87a883`.
This protects state transitions, short-circuit comparisons, cache invalidation,
mask constants, drawing geometry, text sizing, drawable handling and comments.
The existing drawing guard now permits these exact private-name substitutions
while keeping its original baseline-body comparison.

## Coverage and limits

- Focused source/mutation guard: seven deliberately broken variants rejected.
- Two synthetic Android tests are authored: public JVM setter signature checks,
  and drawing/state transitions against the original native drawing View with
  isolated bitmaps and synthetic drawables/text.
- The related state-class audit records finite candidate dispositions separately;
  it is not a replacement for Kotlin typechecking or JVM compilation.

**Kotlin compilation, Android tests and rendered comparison are UNRUN.** The
existing missing-toolchain/artifact-access restriction remains; no Gradle,
artifact download, app controller, production Activity, VPN or traffic was used.
No successful build claim follows from passing source checks.

```sh
python3 scripts/compose-migration/check-watermark-accessors.py --self-test
python3 scripts/compose-migration/check-final-source.py --report docs/compose-migration/watermark-accessor-source-checks.json
# Required once the approved toolchain is present:
./gradlew :app:compileFdroidFullDebugKotlin
./gradlew :app:testFdroidFullDebugUnitTest --tests '*RpnFlagWatermarkStateTest'
```
