Total Ticks: $n \text{ seconds} \times 20 \text{ ticks/sec} = \mathbf{t \text{ ticks}}$.

Solve for JSON Value: $\text{processing_time} = t \div 0.6$


```json
{
    "type": "create:mixing",
    "ingredients": [
        { "item": "minecraft:coal" },
        { "item": "minecraft:coal" },
        { "item": "minecraft:coal" },
        { "item": "minecraft:coal" },
        { "item": "minecraft:coal" },
        { "item": "satisfactory:sulfer" },
        { "item": "satisfactory:sulfer" },
        { "item": "satisfactory:sulfer" },
        { "item": "satisfactory:sulfer" },
        { "item": "satisfactory:sulfer" }
    ],
    "results": [
        {
            "item": "satisfactory:compacted_coal",
            "count": 5
        }
    ],
    "processing_time": 400
}
```