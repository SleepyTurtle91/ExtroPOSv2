# F&B Workflow Research & Field Notes

Observations from real-world restaurant environments in Malaysia.

## Common Pain Points
1. **The "Bungkus" Logic**: Customers often decide late if they want takeaway. The UI must handle switching "Dine-in" to "Takeaway" mid-order easily.
2. **Peak Hour Speed**: Kitchen staff don't have time for complex tablet interaction. KDS must use big touch targets and high-contrast status colors.
3. **Connectivity Issues**: Local network P2P is better than cloud for KDS to avoid "Kitchen didn't get the order" complaints when internet is slow.

---

## Technical Considerations
- **IP Port 9100**: Preferred for thermal kitchen printers.
- **WebSocket / Ktor**: For real-time sync between POS Master and KDS tablets.
