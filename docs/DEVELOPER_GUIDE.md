# Developer Guide - Working with ExtroPOS v2

Essential information for adding new features or maintaining the project.

## 1. Modifying the Database

1. Add/Modify Entity in `com.extrotarget.extroposv2.core.data.model`.
2. Add/Modify DAO in `com.extrotarget.extroposv2.core.data.local.dao`.
3. Update `AppDatabase.kt`:
   - Add entity to the `@Database` list.
   - Increment `version`.
   - Add abstract DAO function.
4. Update `DatabaseModule.kt` to provide the new DAO via Hilt.

## 2. Adding a New Screen

1. Define a new `object` in `Screen.kt` with a unique route and icon.
2. Add a `composable` entry in `NavGraph.kt`.
3. If it requires a ViewModel, ensure it is annotated with `@HiltViewModel`.

## 3. Best Practices

- **Avoid Long Files**: Keep files under 300 lines. Split UI components into smaller `@Composable` functions.
- **State Management**: Use `StateFlow` in ViewModels. Collect as state in Compose using `collectAsStateWithLifecycle()` where possible.
- **Financial Accuracy**: Always use the `CurrencyUtils` and `BigDecimal` for any math involving prices or taxes.
- **Localization**: All string literals must be placed in `strings.xml`.

## 4. Verification

- Run `analyze_file` on modified files.
- Ensure `gradle build` passes before committing.
- Check both Portrait and Landscape orientations (POS devices vary).
