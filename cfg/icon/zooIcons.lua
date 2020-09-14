local util = require("cfgGen.util")
local zooIcons = {}

---@class icon.zooIcons
local name2index = {
    id = 1,
    iconList = 2,
    assetList = 3,
}

local name2ref = {}

local data = {
    [1] = { 1, {"dog Icon","catIcon",}, {1,2,1}, },
}

---@return asset.asset
function zooIcons.get(id)
    return data[id]
end

util.setMetaGet(data, name2index, name2ref)

return zooIcons