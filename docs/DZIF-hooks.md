# DZIF hook map

Source: the supplied HoneyBoard.apk, SHA-256
`6a011ff31270a53dcc53b891009956a13f4de1ba3d271a6ca4316a6215fae840`.
JADX 1.5.2 was used for targeted inspection. 45 whole-APK decompilation errors
were reported; the specific signatures and policy paths below were inspected.
No proprietary code or APK is included in the repository.

| Component | Runtime symbol | Purpose |
|---|---|---|
| Service | HoneyBoardService.onStartInputView(EditorInfo, boolean), onDestroy() | Current InputConnection |
| Toolbar refresh | BeeWorld.X0(List) | Copy incoming bee list, append independent action bees |
| Refresh caller | BeeHiveComponent.G2() | Deoptimized to prevent bypass of the refresh hook |
| Toolbar context | BeeWorld.N() | Obtain native context |
| Bee interface | ue.b | Native actions implemented through a proxy |
| Bee information | ue.j and ue.j$a | Native icon, label, description |
| Builder fields | ue.j$a.f, g, h | Literal label, description resource, description text |
| Native order | im.a.d(), im.b.d() | Add new tags; retain saved customization |
| Tag | BeeTag(String, boolean) | Native toolbar order entries |
| Capacity constant | m1.c.b | Stock condition chooses 200 or 40 |
| Trim policy | u1.f0.q(u1.f0, Clip) | Origin replacement and capacity eviction, before insertion |
| Insertion coroutine | u1.i.invokeSuspend(Object) | Deoptimized so it calls the hooked trim policy |
| Repository storage | field typed k2.r | Find store without guessed field offsets |
| DAO | field typed k2.a, a(int) | Query user entries, ordered by timestamp |
| Delete | k2.r.a(long) | Native row and media cleanup |
| Notify | u1.f0.i(int) | Notify native content observers |
| Settings visibility | CommonSettingsFragmentCompat.isPreferenceVisible(String) | Reveal individually chosen preferences |

Stock q reads the per-user DAO list and removes the first unpinned item at the
stock limit. Origin 1/2 insertion also removes prior items with matching origin.
The adapter changes these two policies, not DAO query results or timestamps.
Pinned items and other users are never policy victims. Retention does not rely
on pinning every clip, changing timestamps, altering schemas, or a shadow database.

The provider accepts explicit `METHOD_REMOVE_CLIP_ITEMS`,
`METHOD_REMOVE_REMOTE_CLIP` and individual deletion requests. These are retained:
their purpose cannot safely be classified as time expiry based on this APK alone.

The exact base-APK identity is checked before obfuscated hooks are installed.
Each feature reports installation separately; first execution emits a separate
stage marker. Framework/remote preference availability is required. The UI does
not label a hook as device-verified merely because it was installed.
